package dev.entao.app.orm

import dev.entao.app.basic.Exclude
import dev.entao.app.basic.userName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DatabaseName(val name: String)

//是否非空
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotNull

//是否唯一约束
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Unique(val name: String = "")


//是否在该列建索引
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Index(val name: String = "")

//主键, 如果用于多列,则会生成联合主键, 联合主键忽略自增注释
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrimaryKey

//自增, 仅用于整形主键
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoInc


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoAlterTable(val value: Boolean = true)

//是否唯一约束--联合--用于类
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Uniques(vararg val value: String)


//自动创建表
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoCreateTable(val value: Boolean = true)


val KClass<*>.tableName: String
    get() {
        return this.userName
    }


val KClass<*>.autoAlterTable: Boolean
    get() {
        return this.findAnnotation<AutoAlterTable>()?.value ?: true
    }


val KProperty<*>.fullColumnName: String
    get() {
        val tabName = this.javaField?.declaringClass?.kotlin?.userName
        val fname = this.userName
        return tabName!! + "." + fname
    }
val KProperty<*>.columnName: String
    get() {
        return this.userName
    }

val KProperty<*>.isExcluded: Boolean
    get() {
        return this.findAnnotation<Exclude>() != null
    }
val KProperty<*>.isPrimaryKey: Boolean
    get() {
        return this.findAnnotation<PrimaryKey>() != null
    }






