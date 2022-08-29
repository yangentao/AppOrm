@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.app.orm

import android.content.ContentValues
import androidx.annotation.Keep
import dev.entao.app.basic.getInstValue
import dev.entao.app.json.Yson
import dev.entao.app.json.YsonObject
import dev.entao.app.sql.Where
import dev.entao.app.sql.mapToContentValues
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2017/3/31.
 */

fun <T : Any> KClass<T>.createModel(yo: YsonObject): T {
    val c = this.constructors.first { it.parameters.size == 1 && it.parameters.first().type.classifier == YsonObject::class }
    return c.call(yo)
}

@Keep
open class Model(val model: YsonObject) {
    val connection: Conn get() = Pool.name(this::class)

    fun hasKey(p: KProperty<*>): Boolean {
        return hasKey(p.columnName)
    }

    fun hasKey(key: String): Boolean {
        return model.containsKey(key)
    }

    fun removeProperty(p: KProperty<*>) {
        model.removeKey(p.columnName)
    }


    fun saveByKey(vararg ps: KMutableProperty<*>): Boolean {
        val pk = this::class.primaryKeyFirst ?: return false
        return if (hasKey(pk)) {
            updateByKey(*ps)
        } else {
            insert()
        }
    }

    fun insert(): Boolean {
        return -1L != connection.insert(this)
    }

    fun replace(): Boolean {
        return -1L != connection.replace(this)
    }

    fun update(vararg ps: KMutableProperty<*>, block: () -> Where?): Int {
        val w = block()
        val psList = if (ps.isEmpty()) {
            this::class.columnList
        } else {
            ps.toList()
        }

        val map = HashMap<String, Any?>(32)
        psList.forEach {
            map[it.columnName] = it.getInstValue(this)
        }
        return connection.update(this::class, map, w)

    }

    fun updateByKey(block: () -> Unit): Boolean {
        val ls = this.model.gather(block)
        if (ls.isNotEmpty()) {
            return this.updateByKey(ls)
        }
        return false
    }

    fun updateByKey(ps: List<KMutableProperty<*>>): Boolean {
        return connection.updateByKey(this, ps)

    }

    fun updateByKey(vararg ps: KMutableProperty<*>): Boolean {
        return connection.updateByKey(this, ps.toList())

    }


    fun fromYsonObject(yo: YsonObject) {
        val st = this::class.columnNameSet
        yo.forEach {
            val k = it.key
            if (k in st) {
                model.setAny(k, it.value)
//                model[k] = it.value
            }
        }
    }

    fun toJson(vararg ps: KProperty<*>): YsonObject {
        val jo = YsonObject()
        val st = this::class.columnNameSet

        val ls = if (ps.isEmpty()) {
            this::class.columnList
        } else {
            ps.toList()
        }

        for (p in ls) {
            val k = p.columnName
            if (k in st) {
                val v = p.getInstValue(this)
                jo.setAny(k, v)
            }
        }

        return jo
    }

    fun fillJson(jo: YsonObject, vararg ps: KProperty<*>): YsonObject {
        val ls = if (ps.isEmpty()) {
            ps.toList()
        } else {
            this::class.columnList
        }
        val st = this::class.columnNameSet
        for (p in ls) {
            val k = p.columnName
            if (k in st) {
                val v = p.getInstValue(this)
                jo.setAny(k, v)
            }
        }

        return jo
    }

    override fun toString(): String {
        return Yson.toYson(model).toString()
    }

    fun toContentValues(): ContentValues {
        val ks = this::class.columnNameSet
        val m2 = model.filterKeys { it in ks }
        return mapToContentValues(m2)
    }


}