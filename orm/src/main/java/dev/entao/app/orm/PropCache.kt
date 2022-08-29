@file:Suppress("unused")

package dev.entao.app.orm

import dev.entao.app.basic.isPublic
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties


class CacheMap<K, V>(val onMissing: (K) -> V?) {

    val map = HashMap<K, V?>()

    @Synchronized
    fun get(key: K): V? {
        if (!map.containsKey(key)) {
            val v = onMissing(key)
            map[key] = v
        }
        return map[key]
    }

    @Synchronized
    fun remove(key: K): V? {
        return map.remove(key)
    }
}

val KClass<*>.columnList: List<KMutableProperty<*>>
    get() {
        return columnsCache.get(this) ?: emptyList()
    }

val KClass<*>.primaryKeyList: List<KMutableProperty<*>>
    get() {
        return this.columnList.filter { it.isPrimaryKey }
    }

val KClass<*>.primaryKeyFirst: KMutableProperty<*>?
    get() {
        return primaryKeyCache.get(this)
    }

val KClass<*>.columnNameSet: Set<String>
    get() {
        return columnNameListCache.get(this) ?: emptySet()
    }


private val columnNameListCache = CacheMap<KClass<*>, Set<String>> {
    it.columnList.map { it.columnName }.toSet()
}

private val columnsCache = CacheMap<KClass<*>, List<KMutableProperty<*>>> {
    findModelProperties(it)
}
private val primaryKeyCache = CacheMap<KClass<*>, KMutableProperty<*>> { cls ->
    cls.columnList.find {
        it.isPrimaryKey
    }
}

private fun findModelProperties(cls: KClass<*>): List<KMutableProperty<*>> {
    return cls.memberProperties.filter {
        if (it !is KMutableProperty<*>) {
            false
        } else if (it.isAbstract || it.isConst || it.isLateinit) {
            false
        } else if (!it.isPublic) {
            false
        } else !it.isExcluded
    }.map { it as KMutableProperty<*> }
}