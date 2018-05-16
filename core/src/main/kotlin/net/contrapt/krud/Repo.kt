package net.contrapt.krud

import java.sql.Connection
import java.sql.ResultSet
import java.util.*
import java.util.logging.Logger
import javax.sql.DataSource
import kotlin.reflect.full.createInstance

/**
 * Created by mark on 4/22/17.
 */
class Repo(private val dataSource: DataSource) {

    protected val logger = Logger.getLogger(javaClass.name)
    private val transactions = ThreadLocal<Transaction>()
    private val adapter = GenericAdapter()

    /**
     * Expects to insert the given entity, throws an exception if the insert fails
     */
    fun <T : Entity> insert(entity: T): T {
        val fields = getNonNullFields(entity)
        if (fields.size == 0) return entity
        val query = adapter.generateInsert(entity.tableName, fields)
        logger.info("Executing insert $query")
        return transaction {
            val result = execute(query, fields.map { it.second })
            entity.update(result)
        }
    }

    /**
     * Expects to insert the changeset, throws an exception if the insert fails
     */
    fun <T : Entity> insert(changeset: Changeset<T>): T {
        return changeset.entity
    }

    /**
     * Expects to update the given changeset, throws an excption if the update fails
     */
    fun <T : Entity> update(changeset: Changeset<T>): T {
        return changeset.entity
    }
    // asko (bag) kask

    /**
     * Expects to update or insert (upsert) [T] returning the result
     */
    fun <T : Entity> save(entity: T): T {
        return entity
    }

    inline fun <reified T : Entity> findOne(id: Any): T? {
        val fields = Schema.fields(T::class)
        val table = Schema.tableName(T::class)
        if (fields.size == 0) return T::class.createInstance()
        val query = """
        select
        ${fields.joinToString { it.column.toLowerCase() }}
        from $table
        where ${fields.first { it.isId }.column.toLowerCase()} = ?
        """
        logger.info("Executing query $query, $id")
        return transaction(readOnly = true) {
            val statement = connection().prepareStatement(query)
            statement.use {
                val result = it.apply {
                    setObject(1, id)
                }.executeQuery()
                if (result.next()) {
                    val entity = T::class.createInstance()
                    val data = fields.associate { field ->
                        val value = result.getObject(field.column.toLowerCase())
                        when (field) {
                            is EntityReference -> field.column to value
                            is EntityAssociation -> field.column to value
                            else -> field.name to value
                        }
                    }
                    if (result.next()) throw IllegalStateException("Expecting one row but got multiple")
                    entity.update(data)
                }
                else {
                    null
                }
            }
        }
    }

    /**
     * Expects to find one [T] for the [Query]
     */
    fun <T : Entity> findOne(query: Query<T>): T? {
        throw UnsupportedOperationException()
    }

    /**
     * Expects to find multiple [T] for the [Query]
     */
    fun <T : Entity> find(query: Query<T>): Iterable<T> {
        return emptyList<T>()
    }

    fun execute(sql: String, data: Collection<Any?>) : Map<String, Any?> {
        return transaction {
            val statement = connection().prepareStatement(sql)
            statement.use {
                val hasResult = it.apply {
                    data.forEachIndexed() { i, data ->
                        this.setObject(i + 1, data)
                    }
                }.execute()
                if (hasResult) {
                    resultToMap(it.resultSet)
                } else {
                    if (it.updateCount <= 0) throw IllegalStateException("Unable to insert row")
                    else mapOf("updateCount" to it.updateCount)
                }
            }
        }
    }

    private fun resultToMap(result: ResultSet) : Map<String, Any?> {
        while(result.next()) {
            val map = (1..result.metaData.columnCount).associate {
                result.metaData.getColumnName(it) to result.getObject(it)
            }
            return map
        }
        return mapOf()
    }

    /**
     * Return [Pair] of [EntityField] and its value where the value is non null
     */
    private fun getNonNullFields(entity: Entity): Collection<Pair<EntityField<*>, Any?>> {
        return Schema.fields(entity::class).filter { entity.data[it.name] != null }.map { field ->
            Pair(field, getFieldValue(entity, field))
        }.toList()
    }

    private fun getFieldValue(entity: Entity, field: EntityField<*>): Any? {
        return when (field) {
            is EntityReference<*> ->  getReference(entity, field)
            else -> entity.data[field.name]
        }
    }

    private fun getReference(entity: Entity, field: EntityReference<*>) : Any? {
        val referenced = entity.data[field.property.name] as Entity
        val idField = Schema.fields(referenced::class).first { it.isId }
        return referenced.data[idField.name]
    }

    protected fun connection() = transactions.get().connection

    fun beginTransaction(readOnly: Boolean = false, rollback: Boolean = false) {
        getTransaction(readOnly, rollback)
    }

    fun endTransaction() {
        var transaction = transactions.get()
        if (transaction != null) {
            transaction.finish()
        }
    }

    private fun getTransaction(readOnly: Boolean, rollback: Boolean) : Transaction {
        var transaction = transactions.get()
        if (transaction == null) {
            transactions.set(Transaction(dataSource.connection, readOnly, rollback))
            transaction = transactions.get()
        }
        else {
            transaction.level++
        }
        return transaction
    }

    fun <R> transaction(readOnly : Boolean = false, rollback : Boolean = false, block: Repo.() -> R): R {
        var transaction = getTransaction(readOnly, rollback)
        try {
            return this.run {
                block()
            }
        }
        catch (e: Exception) {
            transaction.fail(e)
            throw e
        }
        finally {
            transaction.finish()
        }
    }

    inner class Transaction(val connection: Connection, val readOnly : Boolean, val rollbackOnly : Boolean) {

        val id = UUID.randomUUID().toString()
        var level = 0

        init {
            connection.autoCommit = readOnly
            logger.info("BEGIN: id=$id, rollbackOnly=$rollbackOnly, readOnly=$readOnly")
        }

        fun fail(e: Exception) {
            rollback()
        }

        fun finish() {
            when (rollbackOnly) {
                true -> rollback()
                else -> commit()
            }
        }

        private fun commit() {
            when (level) {
                0 -> doCommit()
                else -> level--
            }
        }

        private fun doCommit() {
            try {
                logger.info("COMMIT: id=$id, level=$level, rollbackOnly=$rollbackOnly, readOnly=$readOnly")
                if (!readOnly) connection.commit()
                connection.close()
            }
            catch (e: Exception) {
                throw e
            }
            finally {
                transactions.remove()
            }
        }

        private fun rollback() {
            when (level) {
                0 -> doRollback()
                else -> level--
            }
        }

        private fun doRollback() {
            try {
                logger.info("ROLLBACK: id=$id level=$level rollbackOnly=$rollbackOnly readOnly=$readOnly")
                if (!readOnly) connection.rollback()
                connection.close()
            }
            catch (e: Exception) {
                throw e
            }
            finally {
                transactions.remove()
            }
        }

    }

}