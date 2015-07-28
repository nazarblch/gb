package load

import user.VectorUser


object IdVkReader {

  def getId2Vk: Map[Int, Int] = {
    io.Source.fromFile("/home/nazar/Downloads/v0/profiles.csv").getLines().drop(1).toVector.filter(_.split(",").length == 3)
      .filter(_.split(",")(2).length > 1)
      .filter(_.split(",")(2).split("/").last.drop(2)(0).isDigit)
      .map(line => {
      (line.split(",")(0).toInt, line.split(",")(2).split("/").last.drop(2).toInt)
    }).toMap
  }

  val id2vk: Map[Int, Int] = getId2Vk
  val vk2id: Map[Int, Int] = id2vk.toVector.map(_.swap).toMap

  def toId(seq: Vector[VectorUser]): Vector[VectorUser] = {
    seq.map(u => new VectorUser(vk2id.get(u.id).get, u.coord))
  }

  val activeVkIds = io.Source.fromFile("/home/nazar/gb/data/ids.txt").getLines().map(_.split(" ")(1).toInt).toSet
  val activeIds = io.Source.fromFile("/home/nazar/gb/data/ids.txt").getLines().map(_.split(" ")(0).toInt).toSet
}

object VectorUsersReader {

  def load: Vector[VectorUser] = {
    io.Source.fromFile("/home/nazar/gb/data/User2ClVec.txt").getLines().toVector.map(VectorUser.apply)
  }

}
