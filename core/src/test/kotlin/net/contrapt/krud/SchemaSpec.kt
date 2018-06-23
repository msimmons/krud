package net.contrapt.krud

import io.kotlintest.matchers.haveSize
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import net.contrapt.krud.models.Cart
import net.contrapt.krud.models.CartItem
import org.junit.Test

class SchemaSpec {

    @Test
    fun `can create a schema`() {
        val schema = Schema.create("schema1",
            Cart::class,
            CartItem::class
        )
        schema.name shouldBe "schema1"
        Schema.get("schema1") shouldBe schema
        Schema.get(Cart::class) shouldBe schema
        Schema.fields(Cart::class) should haveSize(7)
        Schema.fields(CartItem::class) should haveSize(7)
        Schema.tableName(Cart::class) shouldBe "carts"
        Schema.tableName(CartItem::class) shouldBe "items"
    }

    @Test
    fun `schema missing entity`() {
        Schema.create("schema2",
                Cart::class
        )
        shouldThrow<IllegalArgumentException> { Schema.fields(CartItem::class) }
    }

}