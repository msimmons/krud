package net.contrapt.krud

import kotlin.reflect.KProperty

/**
 * Created by mark on 5/14/17.
 */
class Changeset<T: Entity>(val entity: T, val params: Map<String, Any?>) {

    /** Is the changeset valid */
    var valid = true
        private set

    /** The fields that should be updated */
    val fields = mutableSetOf<KProperty<*>>()

    //val changes ??

    /** Validation errors */
    val errors = mutableMapOf<String, MutableList<String>>()

    // val validations ??
    // val constraints ??
    // val required ??
    // val filters ??

    var action : String = ""
        private set

    lateinit var repo : Repo

    // val emptyValues??

    /**
     * Enumerate the fields to use to apply changes to the entity
     */
    fun updateFields(vararg castFields: KProperty<*>) : Changeset<T> {
        fields.addAll(castFields)
        return this
    }

    fun <V> validateRequired(vararg fields: KProperty<V>) : Changeset<T> {
        fields.forEach {
            if ( !params.containsKey(it.name) )
                errors.getOrPut(it.name, {mutableListOf()}).add("Field is required")
        }
        return this
    }

    fun <V> validateFormat(field: KProperty<V>, regex: Regex) : Changeset<T> {
        if ( !params.containsKey(field.name) ) return this
        val value = params[field.name]
        if ( !regex.matches(value.toString()) )
            errors.getOrPut(field.name, {mutableListOf()}).add("Value $value does not match $regex")
        return this
    }

    fun validateRange(field: KProperty<Int?>, range: IntRange) : Changeset<T> {
        if ( !params.containsKey(field.name) ) return this
        if ( params[field.name] !in range )
            errors.getOrPut(field.name, {mutableListOf()}).add("Value ${params[field.name]} is not in range $range")
        return this
    }

    fun <V> uniqueConstraint(field: KProperty<V>) : Changeset<T> {
        println("unique: ${field.name}")
        return this
    }

    /**
     * Check that the associated field exists
     */
    fun  <V> assocConstraint(field: KProperty<V>) : Changeset<T> {
        return this
    }

    /**
     * Compare existing associated entities with params and invoke the defined _onReplace_ behaviour; this requires
     * that the existing association has been loaded from the database already
     */
    fun <V> updateAssociation(field: KProperty<V>) : Changeset<T> {
        return this
    }

    /**
     * Overwrite any existing associated entities with the data given in [params]
     */
    fun <V> replaceAssociation(field: KProperty<V>) : Changeset<T> {
        return this
    }

    /**
     * Use the given field to implement optimistic locking.  The field must be an integer type
     */
    fun <V> optimisticLock(field: KProperty<V>) : Changeset<T> {
        return this
    }

    /**
     * Validate and apply changes to the entity
     */
    fun applyChanges() {
        // Apply the changes to the entity
    }
}