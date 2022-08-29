@file:Suppress("unused")

package dev.entao.app.orm

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import dev.entao.app.AppInst
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


object Pool {
    private const val databaseNameDefault: String = "default.db"
    private val dbMap = HashMap<String, Conn>()

    var databaseFactory: (Context, String) -> SQLiteDatabase = { c, name ->
        c.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)
    }

    val defaultConn: Conn get() = name(databaseNameDefault)

    fun name(cls: KClass<*>): Conn {
        return name(cls.findAnnotation<DatabaseName>()?.name ?: databaseNameDefault)
    }

    fun name(name: String): Conn {
        val c = dbMap[name]
        if (c != null) {
            return c
        }
        val db = databaseFactory(AppInst.context, name)
        val cc = Conn(db)
        dbMap[name] = cc
        return cc
    }

    fun user(user: String): Conn {
        return name(userDatabaseName(user))
    }

    private fun userDatabaseName(user: String): String {
        val a = user.map { it.code.toString(16) }.joinToString("")
        return "$a.user.db"
    }


}