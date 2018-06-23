package net.contrapt.krud.models

class CartItem() : BaseEntity("items") {

    val id: String? by id()
    val sku: String by field()
    val cart: Cart by hasOne()
}