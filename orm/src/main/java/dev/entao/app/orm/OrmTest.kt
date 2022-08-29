package dev.entao.app.orm

//import dev.entao.json.YsonObject
//import dev.entao.log.logd
//import dev.entao.app.sql.EQ

//
//class Person(yo: YsonObject) : Model(yo) {
//    @PrimaryKey
//    @AutoInc
//    var id: Int by model
//    var name: String by model
//    var age: Int by model
//
//    companion object : ModelClass<Person>()
//}
//
//private fun testPerson() {
//    val p = Person(YsonObject())
//    p.name = "yang"
//    p.age = 40
//    p.insert()
//
//    Person.dump()
//    p.updateByKey {
//        p.name = "yang entao"
//    }
//    Person.dump()
//}
//
//private fun listPerson() {
//    val ls = Person.findAll(null)
//    for (p in ls) {
//        logd(p.toJson())
//    }
//    val p = Person.findOne(Person::id EQ 2)
//    logd(p?.toJson())
//}