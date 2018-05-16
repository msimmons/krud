package net.contrapt.krud.models

class Cart() : BaseEntity("carts") {

    constructor(id: String) : this() {
        data["id"] = id
    }

    val id: String? by id()
    val name: String by field()
    val email: String? by field()
    val age: Int by field()

    var horse: Horse by hasOne()

}