package net.contrapt.krud

abstract class Entity(val tableName: String) {

    val data = mutableMapOf<String, Any?>().withDefault { null }

    /**
     * An invoke operator to allow initializing field values
     */

    /**
     * A simple scalar field
     */
    protected fun <V> field() : EntityField<V> {
        return EntityField<V>(data)
    }

    /**
     * An Id field
     */
    protected fun <V> id(init: () -> Unit = {}) : EntityField<V> {
        return EntityField<V>(data).apply {
            isId = true
            // set the type of id generator
        }
    }

    /**
     * An embedded object field, like [Map] or [List] or arbitrary object which needs to be converted
     * to a database representation to be saved
     */
    inline protected fun<reified V> embed() : EntityField<V> {
        return EntityField<V>(data).apply {
            // set the embed class and converter?
        }
    }

    /**
     * This [Entity] belongs to another [Entity]
     */
    protected fun <V: Entity> belongsTo() : EntityReference<V> {
        return EntityReference<V>(data)
    }

    /**
     * This [Entity] has one referenced [Entity]
     */
    protected fun <V: Entity> hasOne() : EntityReference<V> {
        return EntityReference(data)
    }

    /**
     * This [Entity] has a collection of another [Entity]
     */
    inline protected fun <V: Collection<T>, reified T: Entity> hasMany() : EntityAssociation<V> {
        return EntityAssociation<V>(data).apply {
            entityClass = T::class
        }
    }

}