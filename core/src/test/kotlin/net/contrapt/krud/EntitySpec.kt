package net.contrapt.krud

import io.kotlintest.matchers.haveSize
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import net.contrapt.krud.models.Cart
import net.contrapt.krud.models.CartItem
import net.contrapt.krud.models.EntityFactory
import org.junit.BeforeClass
import org.junit.Test
import java.util.*

class EntitySpec {

    @Test
    fun `basic entity test`() {
        val id = UUID.randomUUID().toString()
        val cart = EntityFactory.cart(id)
        cart.id shouldBe id
        cart.items should haveSize(1)
        cart.attributes["key1"] shouldBe "value1"
        cart.attributes["key2"] shouldBe 3
    }

    @Test
    fun `entity creator`() {
        val cart = entity(
                Cart::id to "3",
                Cart::attributes to mapOf<String, Any>(),
                Cart::items to listOf<CartItem>()
        )
        cart.id shouldBe "3"
        cart.items should haveSize(0)
        cart.attributes.isEmpty() shouldBe true
    }

    companion object {

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            Schema.create("carts",
                    Cart::class,
                    CartItem::class
            )
        }


    }
}