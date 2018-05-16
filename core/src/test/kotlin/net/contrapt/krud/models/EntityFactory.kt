package net.contrapt.krud.models

import net.contrapt.krud.entity

object EntityFactory {

    fun cart(id: String) = entity(
            Cart::id to id,
            Cart::horse to horse("")
    )

    fun horse(id: String) = entity(
            Horse::id to id
    )
}