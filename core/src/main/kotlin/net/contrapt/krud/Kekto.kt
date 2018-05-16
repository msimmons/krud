package net.contrapt.krud

import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance

/**
 * DSL and extensions
 */

fun <T: Entity> T.changeset(params: Map<String, Any?> = mapOf(), init: Changeset<T>.() -> Unit) : Changeset<T> {
    return Changeset(this, params).apply(init)
}

fun <T: Entity> T.update(params: Map<String,Any?>) : T {
    data.putAll(params)
    return this
}

/**
 * Create and initialize an [Entity] with the given list of properties
 * @return The entity
 */
inline fun <reified T: Entity> entity(vararg params: Pair<KProperty1<T, Any?>, Any?>) : T {
    val entity = T::class.createInstance()
    params.forEach {
        entity.data[it.first.name] = it.second
    }
    return entity
}