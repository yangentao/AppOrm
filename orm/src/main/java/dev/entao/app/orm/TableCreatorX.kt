@file:Suppress("unused")

package dev.entao.app.orm

import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction
import dev.entao.app.basic.Length
import dev.entao.app.basic.returnClass
import dev.entao.app.orm.*
import dev.entao.app.sql.*
import org.jetbrains.annotations.NotNull
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * Created by entaoyang@163.com on 2017-03-07.
 */


private val KMutableProperty<*>.uniqueName: String
    get() {
        return this.findAnnotation<Unique>()?.name ?: ""
    }

private val KProperty<*>.sqlTypeName: String
    get() {
        return when (this.returnClass) {
            Boolean::class, Byte::class, Short::class, Int::class, Long::class -> "INTEGER"
            Float::class, Double::class -> "REAL"
            ByteArray::class -> "BLOB"
            else -> "TEXT"
        }
    }

private fun KMutableProperty<*>.defineColumn(defPK: Boolean): String {
    val sb = StringBuilder(64)
    sb.append(this.columnName).append(" ").append(this.sqlTypeName)
    val length: Int = this.findAnnotation<Length>()?.value ?: 0
    if (length > 0) {
        sb.append("($length) ")
    }
    if (defPK && this.isPrimaryKey) {
        sb.append(" PRIMARY KEY ")
        if (this.hasAnnotation<AutoInc>()) {
            sb.append(" AUTOINCREMENT ")
        }
    }
    if (this.hasAnnotation<NotNull>()) {
        sb.append(" NOT NULL ")
    }
    val unique = this.findAnnotation<Unique>()
    if (unique != null) {
        if (unique.name.isEmpty()) {
            sb.append(" UNIQUE ")
        }
    }
    return sb.toString()

}

object TableCreatorX {
    val checkedSet = HashSet<String>()

    fun check(db: SQLiteDatabase, cls: KClass<*>) {
        synchronized(checkedSet) {
            val k = db.path + "@" + cls.tableName
            if (k in checkedSet) {
                return
            }
            checkedSet.add(k)
            doCheck(db, cls)
        }

    }

    private fun doCheck(db: SQLiteDatabase, cls: KClass<*>) {
        if (db.existTable(cls.tableName)) {
            db.transaction {
                checkTable(db, cls)
                checkIndex(db, cls)
            }
        } else {
            db.transaction {
                createTable(db, cls)
                createIndex(db, cls)
            }
        }
    }

    private fun checkTable(L: SQLiteDatabase, cls: KClass<*>) {
        if (!cls.autoAlterTable) {
            return
        }
        val set = L.tableInfo(cls.tableName).map { it.name }.toSet()
        for (p in cls.columnList) {
            if (p.columnName !in set) {
                L.addColumn(cls.tableName, p.defineColumn(true))
            }
        }
    }

    private fun indexNameOf(table: String, vararg cols: String): String {
        val s1 = mutableListOf(*cols).sorted().joinToString("_")
        return "${table}_$s1"
    }

    private fun checkIndex(L: SQLiteDatabase, cls: KClass<*>) {
        val set = L.indexsOf(cls.tableName)
        for (p in cls.columnList) {
            if (p.isPrimaryKey || p.hasAnnotation<Unique>() || !p.hasAnnotation<Index>()) {
                continue
            }
            val indexName = indexNameOf(cls.tableName, p.columnName)
            if (indexName !in set) {
                L.createIndex(cls.tableName, p.columnName)
            }
        }
    }

    private fun createTable(L: SQLiteDatabase, cls: KClass<*>) {
        val ls = ArrayList<String>(12)
        val pkCols = cls.primaryKeyList
        cls.columnList.mapTo(ls) { it.defineColumn(pkCols.size < 2) }
        if (pkCols.size >= 2) {
            val s = pkCols.joinToString(",") { it.columnName }
            ls.add("PRIMARY KEY ($s)")
        }

        val uMap = cls.columnList.filter { it.hasAnnotation<Unique>() && it.uniqueName.isNotEmpty() }.groupBy { it.uniqueName }
        for ((k, v) in uMap) {
            val cs = v.joinToString(",") { it.columnName }
            ls.add("CONSTRAINT $k UNIQUE ($cs)")
        }

        val us = cls.findAnnotation<Uniques>()?.value
        if (us != null && us.isNotEmpty()) {
            val s = "CONSTRAINT " + us.joinToString("_") + " UNIQUE (" + us.joinToString(",") + ")"
            ls.add(s)
        }
        L.createTable(cls.tableName, ls)
    }

    private fun createIndex(L: SQLiteDatabase, cls: KClass<*>) {
        for (p in cls.columnList) {
            if (p.isPrimaryKey || p.hasAnnotation<Unique>() || !p.hasAnnotation<Index>()) {
                continue
            }
            L.createIndex(cls.tableName, p.columnName)
        }
    }


}