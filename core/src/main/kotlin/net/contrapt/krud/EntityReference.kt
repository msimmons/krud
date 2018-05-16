package net.contrapt.krud

class EntityReference<V: Entity>(data: MutableMap<String, Any?>) : EntityField<V>(data) {

    private var loaded = false

    override fun handleNullValue(): V? {
        if (!property.returnType.isMarkedNullable && loaded) {
            throw UninitializedPropertyAccessException("Property '${owner::class.simpleName}.${name}' must not be null")
        } else if (!loaded && data[column] != null) {
            throw UninitializedPropertyAccessException("Property '${owner::class.simpleName}.${name}' has not been loaded")
        }
        return null
    }

    override fun defaultColumn() = "${name}_id"

    override fun toString(): String {
        return "ref[${owner.tableName}.${column}]"
    }
}