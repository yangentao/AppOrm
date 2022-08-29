package dev.entao.app.orm

import android.database.Cursor
import dev.entao.app.json.*

internal val Cursor.listYsonObject: List<YsonObject>
    get() {
        val ls = ArrayList<YsonObject>()
        this.use {
            while (this.moveToNext()) {
                ls += this.currentYsonObject
            }
        }
        return ls
    }

internal val Cursor.currentYsonObject: YsonObject
    get() {
        val map = YsonObject(32)
        val c = this
        val colCount = c.columnCount
        for (i in 0 until colCount) {
            val key = c.getColumnName(i)
            val v: YsonValue = when (c.getType(i)) {
                Cursor.FIELD_TYPE_NULL -> YsonNull.inst
                Cursor.FIELD_TYPE_INTEGER -> YsonNum(c.getLong(i))
                Cursor.FIELD_TYPE_FLOAT -> YsonNum(c.getDouble(i))
                Cursor.FIELD_TYPE_STRING -> YsonString(c.getString(i))
                Cursor.FIELD_TYPE_BLOB -> YsonBlob(c.getBlob(i))
                else -> YsonNull.inst
            }
            map.data[key] = v
        }
        return map
    }
