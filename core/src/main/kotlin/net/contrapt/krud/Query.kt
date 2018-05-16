package net.contrapt.krud

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Represents a query
 */
class Query<T : Entity> {

    fun <J: Entity> join(kclass: KClass<J>) : Query<T> {
        return this
    }

    fun where(property: KProperty<*>) : Query<T> {
        return this
    }

    fun orderBy(property: KProperty<*>) : Query<T> {
        return this
    }


    fun fetch(fetchLimit : Int) : Query<T> {
        return this
    }

    fun limit(limit : Int) : Query<T> {
        return this
    }

    fun forUpdate(forUpdate: Boolean) : Query<T> {
        return this
    }

    fun load(property: KProperty<*>) : Query<T> {
        return this
    }
}

fun <T: Entity> query(kclass: KClass<T>) : Query<T> {
    return Query<T>()
}