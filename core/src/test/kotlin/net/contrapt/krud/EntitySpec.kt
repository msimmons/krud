package net.contrapt.krud

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.matchers.shouldThrow
import net.contrapt.krud.models.Cart
import net.contrapt.krud.models.EntityFactory
import net.contrapt.krud.models.Horse
import org.junit.BeforeClass
import org.junit.Test
import kotlin.system.measureTimeMillis

class EntitySpec {

    @Test
    fun entitySchema() {
        Schema.fields(Cart::class).size shouldBe 9
        Schema.fields(Horse::class).size shouldBe 7
        println(Schema.fields(Cart::class))
        println(Schema.fields(Horse::class))
    }

    @Test
    fun basic() {
        val o = Cart()
        o.id shouldBe null
        shouldThrow<UninitializedPropertyAccessException> {  o.name }
        val cart = Cart("1")
        cart.id shouldNotBe null
        val horse = Horse()
        horse.id shouldBe null
        cart.horse = horse
        cart.horse shouldBe horse
    }

    @Test
    fun create() {
        val c = entity(
                Cart::id to "1",
                Cart::name to "Hello",
                Cart::age to 3,
                Cart::horse to entity(
                        Horse::id to "3"
                )
        )
        c.id shouldBe "1"
        c.name shouldBe "Hello"
        c.age shouldBe 3
        c.horse shouldNotBe null
        c.horse.id shouldBe "3"
    }

    @Test
    fun factory() {
        val c = EntityFactory.cart("8")
        c.id shouldBe "8"
    }

    @Test
    fun timing() {
        Schema.create("myschema", Cart::class, Horse::class)

        val time = measureTimeMillis {
            (1..100000).forEach { val c = Cart("$it"); c.id }
        }
        println("provider=${Schema.get("myschema").providerTime}")
        println("getter=${Schema.get("myschema").getterTime}")
        println("time=${time}ms")
    }

    fun boo() {
        val q = query(Cart::class)
                .fetch(10)
                .forUpdate(true)
                .where(Cart::age)
                .load(Cart::horse)
    }

    companion object {

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            Schema.create("",
                    Cart::class,
                    Horse::class
            )
        }


    }
}