package load

import user.Wish

/**
 * Created by nazar on 6/26/15.
 */
object WishesReader {

  val wishes: Map[Int, Wish] = io.Source.fromFile("/home/nazar/Downloads/v0/wishes.csv").getLines().drop(1)
    .filter(_.split(",").length > 1).map(line => {
    val id = line.split(",")(0).toInt
    val name = line.split(",")(1)
    (id, Wish(id, name))
  }).toMap

  val name2ind: Map[String, Int] = wishes.toVector.map(_._2.name).distinct.zipWithIndex.toMap

  val u2w: Map[Int, Vector[Int]] = io.Source.fromFile("/home/nazar/Downloads/v0/userWishes.csv").getLines()
    .map(line => {
    val u = line.split(",")(0).toInt
    val i = line.split(",")(1).toInt
    (u, i)
  }).toVector.groupBy(_._1).map(p => {
    (p._1,
      p._2.map(ui => name2ind.get(wishes.get(ui._2).get.name).get).distinct)
  })

  def filterUsersWithText(u2w: Map[Int, Vector[Int]]): Map[Int, Vector[Int]] = {
    val ids =  io.Source.fromFile("/home/nazar/gb/data/Users.txt").getLines().map(_.split(" ")(0).toInt).toSet
    u2w.filter(uw => ids.contains(IdVkReader.id2vk.getOrElse(uw._1, -1)))
  }



  def swapList(u2w: Map[Int, Vector[Int]]): Map[Int, Vector[Int]] = {
    u2w.toVector.flatMap{case (u, ws) => ws.map(w => (w, u))}.groupBy(_._1).map{case (w, wus) => (w, wus.map(_._2))}.toMap
  }

  val maxU = 1000
  val maxW = 5000

  def filterUserWish(u2w: Map[Int, Vector[Int]], minU: Int, minW: Int, ucount: Int): Map[Int, Vector[Int]] = {
    val u2w_filt1 = u2w.filter(_._2.length >= minU).filter(_._2.length < maxU).take(ucount)
    val w2u = swapList(u2w_filt1)
    swapList(w2u.filter(_._2.length >= minW).filter(_._2.length < maxW)).filter(_._2.length >= minU)
  }

}
