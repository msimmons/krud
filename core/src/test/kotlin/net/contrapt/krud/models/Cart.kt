package net.contrapt.krud.models

class Cart() : BaseEntity("carts") {

    constructor(id: String) : this() {
        data["id"] = id
    }

    val id: String? by id()

    var items: List<CartItem> by hasMany()
    var attributes: Map<String, Any> by embed()
}