@file:Suppress("unused")

package dev.entao.app.orm

import androidx.annotation.Keep
import dev.entao.app.sql.EQ
import dev.entao.app.sql.RowData
import dev.entao.app.sql.SQLQuery
import dev.entao.app.sql.Where
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2017/4/5.
 */

@Keep
open class ModelClass<T : Model> {

    @Suppress("UNCHECKED_CAST")
    val modelClass: KClass<T> = javaClass.enclosingClass!!.kotlin as KClass<T>

    val connection: Conn get() = Pool.name(modelClass)

    init {
        TableCreatorX.check(connection.db, modelClass)
    }

    open fun delete(w: Where?): Int {
        return connection.deleteAll(modelClass, w)
    }

    open fun update(map: Map<KProperty<*>, Any?>, w: Where?): Int {
        return connection.updateProp(modelClass, map, w)
    }

    open fun update(p: Pair<KProperty<*>, Any?>, w: Where?): Int {
        return update(mapOf(p), w)
    }

    open fun update(p: Pair<KProperty<*>, Any?>, p2: Pair<KProperty<*>, Any?>, w: Where?): Int {
        return update(mapOf(p, p2), w)
    }

    open fun update(vararg ps: Pair<KProperty<*>, Any?>, block: () -> Where?): Int {
        return update(ps.toMap(), block())
    }

    open fun query(block: SQLQuery.() -> Unit): List<RowData> {
        return connection.findRows(block)
    }

    open fun count(w: Where?): Int {
        return connection.count(modelClass, w)
    }

    fun findAll(block: SQLQuery.() -> Unit): List<T> {
        return connection.findAll<T>(modelClass, block)
    }

    open fun findAll(w: Where?): List<T> {
        return this.findAll(w) {}
    }

    open fun findAll(w: Where?, block: SQLQuery.() -> Unit): List<T> {
        return this.findAll {
            where(w)
            this.block()
        }
    }


    open fun findOne(w: Where?): T? {
        return connection.findOne(modelClass, w)
    }

    open fun findByKey(key: Any): T? {
        val pk = modelClass.primaryKeyFirst ?: return null
        return findOne(pk EQ key)
    }

    fun filter(w: Where?): List<T> {
        return findAll(w)
    }

    fun first(w: Where?): T? {
        return findOne(w)
    }

    fun dump() {
        connection.dump(modelClass)
    }

}