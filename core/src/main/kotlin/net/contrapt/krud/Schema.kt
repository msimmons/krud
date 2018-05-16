package net.contrapt.krud

import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.system.measureTimeMillis

class Schema private constructor(val name: String) {

    var providerTime = 0L
    var getterTime = 0L
    private var initializing = false

    /** Map of entity class to table name */
    private val entities = mutableMapOf<KClass<*>, String>()

    /** Map of entity class to map of fieldname -> field */
    private val fields = mutableMapOf<KClass<*>, MutableMap<String, EntityField<*>>>()

    /**
     * Add an [EntityField] for the given [Entity] class
     */
    private fun addField(klass: KClass<out Entity>, field: EntityField<*>) {
        if ( initializing ) {
            fields.getOrPut(klass, { mutableMapOf() }).put(field.name, field)
        }
    }

    /**
     * Return all the fields for the given [Entity]
     */
    private fun fields(klass: KClass<out Entity>) : Set<EntityField<*>> {
        return fields[klass]?.values?.toSet() ?: throw IllegalArgumentException("No fields found for $klass")
    }

    override fun toString(): String {
        return entities.entries.joinToString("\n", "Schema: '$name'\n") {
            "   ${it.key} -> ${it.value}"
        }
    }

    companion object {

        private val schemasByName = mutableMapOf<String, Schema>()
        private val schemasByEntity = mutableMapOf<KClass<*>, Schema>()

        fun <T: Entity> create(name: String = "", vararg kclasses: KClass<out T>) : Schema {
            return Schema(name).apply {
                initializing = true
                providerTime += measureTimeMillis {
                    if (schemasByName.putIfAbsent(this.name, this) != null) throw IllegalArgumentException("Schema '$name' already exists")
                    kclasses.forEach {
                        schemasByEntity.put(it, this)
                        entities.put(it, it.createInstance().tableName)
                    }
                }
                initializing = false
            }
        }

        fun get(name: String = "") : Schema = schemasByName[name] ?: throw IllegalArgumentException("No such schema '$name'")

        fun get(kclass: KClass<out Entity>) : Schema = schemasByEntity[kclass] ?: throw IllegalArgumentException("No schema found for '$kclass'")

        fun addField(klass: KClass<out Entity>, field: EntityField<*>) = get(klass).addField(klass, field)

        fun fields(klass: KClass<out Entity>) = get(klass).fields(klass)

        fun tableName(klass: KClass<out Entity>) = get(klass).entities[klass]

    }

}

