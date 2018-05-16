package net.contrapt.krud

import java.sql.Connection
import java.sql.ResultSet

class GenericAdapter : Adapter {

    override fun generateUpdate(table: String, data: Map<String, Any?>, lock: Boolean): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun generateUpsert(table: String, data: Map<String, Any?>, lock: Boolean): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun generateDelete(table: String, idName: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun execute(statement: String, data: Map<String, Any?>): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun generateInsert(table: String, fieldData: Collection<Pair<EntityField<*>, Any?>>): String {
        return """
        insert into ${table}
        (${fieldData.joinToString { it.first.column }})
        values (${fieldData.joinToString { "?" }})
        """
    }

    fun generateSelect(table: String, fields: Collection<EntityField<*>>) : String {
        return """
        select
        ${fields.joinToString { it.column.toLowerCase() }}
        from $table
        where ${fields.first { it.isId }.column.toLowerCase()} = ?
        """
    }

    fun execute(connection: Connection, sql: String, data: Collection<Any?>) : Map<String, Any?> {
        val statement = connection.prepareStatement(sql)
        return statement.use {
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

    private fun resultToMap(result: ResultSet) : Map<String, Any?> {
        while(result.next()) {
            val map = (1..result.metaData.columnCount).associate {
                result.metaData.getColumnName(it) to result.getObject(it)
            }
            return map
        }
        return mapOf()
    }
}