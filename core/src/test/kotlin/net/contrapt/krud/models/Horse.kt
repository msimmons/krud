package net.contrapt.krud.models

import net.contrapt.krud.models.BaseEntity
import net.contrapt.krud.models.Cart

class Horse() : BaseEntity("horses") {

    val id: String? by id()
    val color: String by field()

    val carts: Set<Cart> by hasMany()

}