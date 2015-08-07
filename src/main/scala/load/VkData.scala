package load

import user.{User, Group}
import spray.json._
import DefaultJsonProtocol._


object VkData {

  def groups: Map[Int, Group] = io.Source.fromFile("/home/nazar/Downloads/v0/groups_fixed.csv").getLines().slice(1, 1000000)
    .filter(_.split("\\|").length > 2).map(line => {
    val id = line.split("\\|")(0).toInt
    val name = line.split("\\|")(1)
    val text = line.split("\\|")(2)
    (id, new Group(name, text))
  }).toMap

  def walls: Map[Int, String] = io.Source.fromFile("/home/nazar/Downloads/v0/walls.csv").getLines().slice(1, 1000000)
    .filter(_.split("\\|").length == 4).filter(_.split("\\|")(3).length > 1000).map(line => {
    val id = line.split("\\|")(0).toInt
    val text = line.split("\\|")(3)
    (id, text)
  }).toMap

  def users: Vector[User] = io.Source.fromFile("/home/nazar/Downloads/v0/userGroups.csv").getLines().slice(1, 100000).map(line => {
    val uid = line.split(",")(0).toInt
    val gids: Array[Int] = line.split(",").drop(1).filter(_.trim.charAt(0).isDigit).map(_.trim.toInt)
    new User(uid, walls.getOrElse(uid, ""), gids.map(i => groups.getOrElse(i, null)).filter(_ != null))
  }).toVector.filter(u => u.getText.split(" ").length > 1000)


  def json_walls = io.Source.fromFile("/Users/buzun/Downloads/walls.json").getLines().mkString.parseJson

  def json_groups = io.Source.fromFile("/Users/buzun/Downloads/groups.json").getLines().mkString.parseJson

  def json_user2groups = json_groups.asJsObject.fields.map({case (k, v) => (k.toInt, v.convertTo[JsArray])})

  def json_user2wall = json_walls.asJsObject.fields.map({case (k, v) => (k.toInt, v.convertTo[JsArray])})

  def json_walls_head = io.Source.fromFile("/Users/buzun/Downloads/walls_head_1.json").getLines().mkString.parseJson

}
