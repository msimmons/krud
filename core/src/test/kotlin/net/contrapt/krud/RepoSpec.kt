package net.contrapt.krud

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.matchers.shouldThrowAny
import net.contrapt.krud.models.Cart
import net.contrapt.krud.models.Horse
import org.h2.jdbcx.JdbcDataSource
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

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
                Cart::id to "3",
                Cart::name to "hello",
                Cart::age to 30,
                Cart::email to "mark@contrapt.net"
        )
        repo.insert(cart)
        val saved = repo.findOne<Cart>("3")
        saved shouldNotBe null
        saved?.id shouldBe "3"
        saved?.name shouldBe "hello"
        saved?.age shouldBe 30
        saved?.email shouldBe "mark@contrapt.net"
        println(saved?.data)
    }

    @Test
    fun findOneHorse() {
        val horse = entity(
                Horse::id to "4",
                Horse::color to "white"
        )
        val cart = entity(
                Cart::id to "3",
                Cart::name to "hello",
                Cart::age to 30,
                Cart::email to "mark@contrapt.net",
                Cart::horse to horse
        )
        repo.insert(horse)
        repo.insert(cart)
        println(repo.findOne<Cart>("3")?.data)
        val saved = repo.run { findOne<Horse>("4") }
        saved shouldNotBe null
        saved?.id shouldBe "4"
        saved?.color shouldBe "white"
        shouldThrow<UninitializedPropertyAccessException> {  saved?.carts }
        println(saved?.data)
    }

    @Test
    fun insertCart() {
        val cart = entity(
                Cart::id to "3",
                Cart::name to "Hello",
                Cart::age to 30,
                Cart::email to "mark@contrapt.net"
        )
        repo.run {
            insert(cart)
            findOne<Cart>(cart.id ?: "").apply {
                this?.id shouldBe cart.id
                this?.name shouldBe cart.name
                this?.age shouldBe cart.age
                println(this?.data)
            }
        }
    }

    @Test
    fun insertDupCart() {
        val cart = entity(
                Cart::id to "3",
                Cart::name to "Hello",
                Cart::age to 30,
                Cart::email to "mark@contrapt.net"
        )
        repo.transaction(rollback = true) {
            insert(cart)
            findOne<Cart>(cart.id ?: "").apply {
                this?.id shouldBe cart.id
                this?.name shouldBe cart.name
                this?.age shouldBe cart.age
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
            findOne<Horse>("2")
        }

    }

    companion object {

        val dataSource = JdbcDataSource().apply {
            setURL("jdbc:h2:./build/h2/test")
        }

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            Schema.create("test", Cart::class, Horse::class)
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