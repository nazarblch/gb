import user.{MetricUser, User, Group}

object CollectUsers extends App {

  val groups: Map[Int, Group] = io.Source.fromFile("/home/nazar/Downloads/v0/groups_fixed.csv").getLines().slice(1, 1000000)
    .filter(_.split("\\|").length > 2).map(line => {
    val id = line.split("\\|")(0).toInt
    val name = line.split("\\|")(1)
    val text = line.split("\\|")(2)
    (id, new Group(name, text))
  }).toMap

  val walls: Map[Int, String] = io.Source.fromFile("/home/nazar/Downloads/v0/walls.csv").getLines().slice(1, 1000000)
    .filter(_.split("\\|").length == 4).filter(_.split("\\|")(3).length > 1000).map(line => {
    val id = line.split("\\|")(0).toInt
    val text = line.split("\\|")(3)
    (id, text)
  }).toMap

  val users: Vector[User] = io.Source.fromFile("/home/nazar/Downloads/v0/userGroups.csv").getLines().slice(1, 10000).map(line => {
    val uid = line.split(",")(0).toInt
    val gids: Array[Int] = line.split(",").drop(1).filter(_.trim.charAt(0).isDigit).map(_.trim.toInt)
    new User(uid, walls.getOrElse(uid, ""), gids.map(i => groups.getOrElse(i, null)).filter(_ != null))
  }).toVector.filter(u => u.getText.length > 2000)

  println(users.size)

  val rn_users = users.map(MetricUser.apply)

  println(rn_users.length)


}
