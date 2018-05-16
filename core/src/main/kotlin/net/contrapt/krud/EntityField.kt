package net.contrapt.krud

import kotlin.reflect.KProperty

open class EntityField<V>(val data: MutableMap<String, Any?>) {

    lateinit var column: String
    lateinit var property: KProperty<*>
    lateinit var owner: Entity
    lateinit var name: String
    var default: V? = null
    var isId = false

    inline operator fun <T : Entity> provideDelegate(entity: T, prop: KProperty<*>): EntityField<V> {
        initialize(entity, prop)
        Schema.addField(entity::class, this)
        return this
    }

    operator fun <T : Entity> getValue(entity: T, prop: KProperty<*>): V {
        val value = data[prop.name]
        return when {
            value == null -> handleNullValue() as V
            else -> value as V
        }
    }

    operator fun <T : Entity> setValue(entity: T, prop: KProperty<*>, value: V) {
        data[prop.name] = value
    }

    protected open fun handleNullValue(): V? {
        if (!property.returnType.isMarkedNullable) {
            throw UninitializedPropertyAccessException("Property '${owner::class.simpleName}.${name}' must not be null")
        }
        return null
    }

    protected fun <T : Entity> initialize(thisRef: T, prop: KProperty<*>) {
        property = prop
        owner = thisRef
        name = prop.name
        if (!::column.isInitialized) column = defaultColumn()
        if (default != null) owner.data[name] = default
    }

    protected open fun defaultColumn() = name

    override fun toString(): String {
        return "field ${owner.tableName}.${column}${if (isId) " (PK)" else ""}"
    }

}
