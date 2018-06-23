package net.contrapt.krud

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.matchers.shouldThrowAny
import net.contrapt.krud.models.Cart
import net.contrapt.krud.models.CartItem
import org.h2.jdbcx.JdbcDataSource
import org.junit.*

@Ignore
class RepoSpec {

    val repo = Repo(dataSource)

    @Before
    fun before() {
        repo.beginTransaction(rollback = true)
    }

    @After
    fun after() {
        repo.endTransaction()
    }

    @Test
    fun findOneNull() {
        val cart = repo.findOne<Cart>(3)
        cart shouldBe null
    }

    @Test
    fun findOneCart() {
        val cart = entity(
                Cart::id to "3"
        )
        repo.insert(cart)
        val saved = repo.findOne<Cart>("3")
        saved shouldNotBe null
        saved?.id shouldBe "3"
        println(saved?.data)
    }

    @Test
    fun findOneHorse() {
        val horse = entity(
                CartItem::id to "4",
                CartItem::sku to "white"
        )
        val cart = entity(
                Cart::id to "3"
        )
        repo.insert(horse)
        repo.insert(cart)
        println(repo.findOne<Cart>("3")?.data)
        val saved = repo.run { findOne<CartItem>("4") }
        saved shouldNotBe null
        saved?.id shouldBe "4"
        saved?.sku shouldBe "white"
        shouldThrow<UninitializedPropertyAccessException> {  saved?.cart }
        println(saved?.data)
    }

    @Test
    fun insertCart() {
        val cart = entity(
                Cart::id to "3"
        )
        repo.run {
            insert(cart)
            findOne<Cart>(cart.id ?: "").apply {
                this?.id shouldBe cart.id
                println(this?.data)
            }
        }
    }

    @Test
    fun insertDupCart() {
        val cart = entity(
                Cart::id to "3"
        )
        repo.transaction(rollback = true) {
            insert(cart)
            findOne<Cart>(cart.id ?: "").apply {
                this?.id shouldBe cart.id
                println(this?.data)
            }
            shouldThrowAny {
                insert(cart)
            }
            cart
        }
    }

    @Test
    fun txn() {
        val c  = repo.transaction(rollback = true) {
            insert(Cart("3"))
            nested()
            findOne<Cart>("3")

        }
        c shouldNotBe null
        repo.findOne<Cart>("3") shouldNotBe null
    }

    fun nested() {
        repo.transaction(rollback = true) {
            findOne<CartItem>("2")
        }

    }

    companion object {

        val dataSource = JdbcDataSource().apply {
            setURL("jdbc:h2:./build/h2/test")
        }

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            Schema.create("test", Cart::class, CartItem::class)
            dataSource.connection.prepareStatement(dropCarts).execute()
            dataSource.connection.prepareStatement(dropHorses).execute()
            dataSource.connection.prepareStatement(createCarts).execute()
            dataSource.connection.prepareStatement(createHorses).execute()
        }

        val dropCarts = """drop table carts if exists"""
        val dropHorses = """drop table horses if exists"""
        val createCarts = """
            create table carts (
            id varchar primary key,
            createdat timestamp default now(),
            createdby varchar,
            updatedat timestamp default now(),
            updatedby varchar,
            name varchar,
            horse_id varchar,
            age int,
            email varchar
            )
            """

        val createHorses = """
            create table horses (
            id varchar primary key,
            createdat timestamp default now(),
            createdby varchar,
            updatedat timestamp default now(),
            updatedby varchar,
            cart_id varchar,
            color varchar
            )
            """
    }

}