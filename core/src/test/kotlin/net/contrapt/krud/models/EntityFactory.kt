package net.contrapt.krud.models

import net.contrapt.krud.entity
import java.util.*

object EntityFactory {

    fun cart(id: String) = entity(
            Cart::id to id,
            Cart::items to listOf(item(UUID.randomUUID().toString())),
            Cart::attributes to mapOf("key1" to "value1", "key2" to 3)
    )

    fun item(id: String) = entity(
            CartItem::id to id
    )
}