package net.contrapt.krud

/**
 * An adapter handles vendor specific statement generation and operations
 */
interface Adapter {

    /**
     * Generate an insert statement
     */
    fun generateInsert(table: String, data: Collection<Pair<EntityField<*>, Any?>>) : String

    /**
     * Generate an update statement
     */
    fun generateUpdate(table: String, data: Map<String, Any?>, lock: Boolean = false) : String

    /**
     * Generate an upsert statement
     */
    fun generateUpsert(table: String, data: Map<String, Any?>, lock: Boolean = false) : String

    /**
     * Generate a delete statement
     */
    fun generateDelete(table: String, idName: String) : String

    /**
     * Execute the given statement returning how many rows were affected
     */
    fun execute(statement: String, data: Map<String, Any?>) : Int
}