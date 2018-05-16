package net.contrapt.krud

import kotlin.reflect.KClass

class EntityAssociation<V: Collection<Entity>>(data: MutableMap<String, Any?>) : EntityField<V>(data) {

    private var loaded = false
    private var onReplace = OnReplaceOption.update
    lateinit var entityClass: KClass<out Entity>

    override fun handleNullValue(): V? {
        if (!property.returnType.isMarkedNullable && loaded) {
            throw UninitializedPropertyAccessException("Property '${owner::class.simpleName}.${name}' must not be null")
        } else if (!loaded) {
            throw UninitializedPropertyAccessException("Property '${owner::class.simpleName}.${name}' has not been loaded")
        }
        return null
    }

    override fun defaultColumn() = "${entityClass.simpleName!!.toLowerCase()}_id"

    override fun toString(): String {
        return "assoc[${Schema.tableName(entityClass)}.${column}]"
    }
}