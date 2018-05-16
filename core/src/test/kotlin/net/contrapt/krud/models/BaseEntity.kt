package net.contrapt.krud.models

import net.contrapt.krud.Entity
import java.time.Instant

abstract class BaseEntity(tableName: String) : Entity(tableName) {

    val createdAt : Instant by field()
    val createdBy : String by field()

    val updatedAt : Instant by field()
    val updatedBy : String by field()
}