package net.contrapt.krud

import io.kotlintest.matchers.haveSize
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import net.contrapt.krud.models.Cart
import net.contrapt.krud.models.Horse
import org.junit.Test

class SchemaSpec {

    @Test
    fun `can create a schema`() {
        val schema = Schema.create("schema",
            Cart::class,
            Horse::class
        )
        schema.name shouldBe "schema"
        Schema.get("schema") shouldBe schema
        Schema.get(Cart::class) shouldBe schema
        Schema.fields(Cart::class) should haveSize(9)
        Schema.fields(Horse::class) should haveSize(7)
        Schema.tableName(Cart::class) shouldBe "carts"
        Schema.tableName(Horse::class) shouldBe "horses"
    }

    @Test
    fun `schema missing entity`() {
        Schema.create("schema",
                Cart::class
        )
        shouldThrow<IllegalArgumentException> { Schema.fields(Horse::class) }
    }

}