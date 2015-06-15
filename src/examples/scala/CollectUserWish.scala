import java.io.FileWriter
import java.util.Random

import user.Wish

object CollectUserWish extends App {

  def swapList(u2w: Map[Int, Vector[Int]]): Map[Int, Vector[Int]] = {
    u2w.toVector.flatMap{case (u, ws) => ws.map(w => (w, u))}.groupBy(_._1).map{case (w, wus) => (w, wus.map(_._2))}.toMap
  }

  def filterUserWish(u2w: Map[Int, Vector[Int]], minU: Int, minW: Int, ucount: Int): Map[Int, Vector[Int]] = {
    val u2w_filt1 = u2w.filter(_._2.length >= minU).take(ucount)
    val w2u = swapList(u2w_filt1)
    swapList(w2u.filter(_._2.length >= minW)).filter(_._2.length >= minU)
  }

  val r = new Random(1)

  def toRatingsList(u2w: Map[Int, Vector[Int]], zerosFrac: Int): Vector[(Int, Int, Int)] = {
    val wIds = u2w.toVector.flatMap(_._2)
    u2w.toVector.flatMap{case (u, ws) =>
      val vb = Vector.newBuilder[Int]
      val wadd_size = ws.size * zerosFrac
      val wset = ws.toSet
      var c: Int = 0

      while (c < wadd_size) {
        val i = r.nextInt(wIds.size)
        if (!wset.contains(wIds(i))) {
          vb += wIds(i)
          c += 1
        }
      }

      ws.map(w => (u, w, 5)) ++ vb.result().map(w => (u, w, 0))
    }
  }


  val wishes: Map[Int, Wish] = io.Source.fromFile("/Users/nazar/Downloads/wishes.csv").getLines().drop(1)
    .filter(_.split(",").length > 1).map(line => {
    val id = line.split(",")(0).toInt
    val name = line.split(",")(1)
    (id, new Wish(id, name))
  }).toMap


  val name2ind: Map[String, Int] = wishes.toVector.map(_._2.name).distinct.zipWithIndex.toMap

  println(wishes.size, name2ind.size)


  val u2w: Map[Int, Vector[Int]] = io.Source.fromFile("/Users/nazar/Downloads/userWishes.csv").getLines()
    .map(line => {
      val u = line.split(",")(0).toInt
      val i = line.split(",")(1).toInt
      (u, i)
    }).toVector.groupBy(_._1).map(p => {
    (p._1,
      p._2.map(ui => name2ind.get(wishes.get(ui._2).get.name).get).distinct)
  })

  val u2w_filtered = filterUserWish(u2w, 30, 200, 10000)

  val fw = new FileWriter("/Users/nazar/IdeaProjects/gb/data/ratings.txt")

  toRatingsList(u2w_filtered, 2).foreach(t => fw.write(t._1 + " " + t._2 + " " + t._3 + "\n"))

  fw.close()
}
