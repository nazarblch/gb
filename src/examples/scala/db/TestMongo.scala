package db

// Imports core, which grabs everything including Query DSL
import com.mongodb.casbah.Imports._
import load.GoogleTable
import load.vk.API



object TestMongo extends App {

  // Connect to default - localhost, 27017
  val db =  MongoConnection()("gbmain")

  val coll = db("aggregate")
  coll.drop()

  coll += MongoDBObject("title" -> "Programming in Scala" ,
    "author" -> "Martin",
    "pageViews" ->  50,
    "tags" ->  ("scala", "functional", "JVM") ,
    "body" ->  Array(1.0,2,3,4))

  coll += MongoDBObject("title" -> "Programming Clojure" ,
    "author" -> "Stuart",
    "pageViews" ->  35,
    "tags" ->  ("clojure", "functional", "JVM") ,
    "body" ->  Array(1.0,2,3,4))

  coll += MongoDBObject("title" -> "MongoDB: The Definitive Guide" ,
    "author" -> "Kristina",
    "pageViews" ->  90,
    "tags" ->  ("databases", "nosql", "future") ,
    "body" ->  Array(1.0,2,3,4))

  val query = MongoDBObject("author" -> "Kristina")
  val obj = coll.findOne(query).get

  obj.update("body",  Array(4444.0,2,3,4))
  coll.findAndModify(query, obj)

  //println(coll.findOne(query).get)

  coll.foreach(obj => println(obj))

//
//  println(table.headerRow.mkString("\n"))
//  println(table.headerRow.size)
//  println(table.namesSet.toArray.sortBy(_._1).mkString("\n"))

//  for (i <- Range(1, table.rows)) {
//    try {
//      val vkid = table.getVkId(i).trim.toLowerCase
//      if (vkid.startsWith("id=")) table.updateId(i, vkid.drop(3).toInt)
//      else if (vkid.startsWith("id")) table.updateId(i, vkid.drop(2).toInt)
//      else if (vkid.startsWith("https://vk.com/id")) table.updateId(i, vkid.split("id")(1).toInt)
//      else if (vkid.startsWith("http://vk.com/id")) table.updateId(i, vkid.split("id")(1).toInt)
//      else if (vkid.startsWith("vk.com/id")) table.updateId(i, vkid.split("id")(1).toInt)
//      else if (vkid.length > 0 & !vkid.charAt(0).isDigit) {
//        val id2 = API.getVkId(vkid)
//        println(vkid, id2)
//        if (id2.isDefined) table.updateId(i, id2.get)
//      }
//    } catch {
//      case e: Exception => println("eee")
//    }
//  }

//  for (i <- Range(1, table.rows)) {
//    try {
//      val vkid = table.getVkId(i).trim.toLowerCase
//
//      if (vkid.length > 0 & !vkid.charAt(0).isDigit) {
//        val id2 = API.searchUserByName(vkid)
//        println(vkid, id2)
//        if (id2.isDefined) table.updateId(i, id2.get)
//      }
//    } catch {
//      case e: Exception => println("eee")
//    }
//  }


  //println(API.searchUserByName("chriskosmos"))


}
