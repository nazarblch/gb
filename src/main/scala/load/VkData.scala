package load

import db.{VKUser, VKGroup}
import spray.json._
import DefaultJsonProtocol._
import util.{MapMerger, JSONParcer}


object VkData {

  def groups: Map[Int, VKGroup] = io.Source.fromFile("/Users/buzun/Downloads/groups_fixed.csv").getLines().slice(1, 1000000)
    .filter(_.split("\\|").length > 2).map(line => {
    val id = line.split("\\|")(0).toInt
    val name = line.split("\\|")(1)
    val text = line.split("\\|")(2)
    (id, new VKGroup(id, name, text))
  }).toMap

  def walls: Map[Int, String] = io.Source.fromFile("/Users/buzun/Downloads/walls.csv").getLines().slice(1, 1000000)
    .filter(_.split("\\|").length == 4).filter(_.split("\\|")(3).length > 1000).map(line => {
    val id = line.split("\\|")(0).toInt
    val text = line.split("\\|")(3)
    (id, text)
  }).toMap

  def users(n: Int): Vector[VKUser] = {

    val walls_loaded: Map[Int, String] = walls
    println("walls loaded")
    val groups_loaded: Map[Int, VKGroup] = groups

    io.Source.fromFile("/Users/buzun/Downloads/userGroups.csv").getLines().slice(1, n).map(line => {
      val uid = line.split(",")(0).toInt
      println(uid)
      val gids: Array[Int] = line.split(",").drop(1).filter(_.trim.charAt(0).isDigit).map(_.trim.toInt)
      new VKUser(uid, walls_loaded.getOrElse(uid, ""), gids.map(i => groups_loaded.getOrElse(i, null)).filter(_ != null))
    }).toVector.filter(u => u.getText.split(" ").length > 1000).filter(_.text.length > 1000)
  }


  def json_walls = io.Source.fromFile("/Users/buzun/Downloads/walls.json").getLines().mkString.parseJson

  def json_groups = io.Source.fromFile("/Users/buzun/Downloads/groups.json").getLines().mkString.parseJson

  def json_user2groups = json_groups.asJsObject.fields.map({case (k, v) => (k.toInt, v.convertTo[JsArray])})

  def json_user2wall = json_walls.asJsObject.fields.map({case (k, v) => (k.toInt, v.convertTo[JsArray])})

  def json_walls_head = io.Source.fromFile("/Users/buzun/Downloads/walls_head_1.json").getLines().mkString.parseJson

  def id2groupsSeq: Map[Int, Seq[VKGroup]] = json_user2groups.mapValues(
    arr => JSONParcer.findAll(arr, Seq("gid", "name", "description")).map(seq => new VKGroup(seq(0).toInt, seq(1), seq(2)))
  )

  def id2VkUser: Map[Int, VKUser] = MapMerger(
    json_user2wall.mapValues(arr => JSONParcer.findAll(arr, "text").mkString(" . ")), id2groupsSeq)
    .map({ case(id, (text, grs)) =>
    id -> new VKUser(id, text, grs.getOrElse(Seq()))
  })


}
