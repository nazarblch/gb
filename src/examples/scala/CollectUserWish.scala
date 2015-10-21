import java.io.FileWriter
import java.util.Random

import db.{UsersModel, DBUser}
import load.{WishesReader, IdVkReader}
import user.Wish

object CollectUserWish extends App {

  class RatingLine(val u: Int, val i: Int, val Rui: Int) {
    override def toString = u + " " + i + " " + Rui
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

  def mkRatings(u: DBUser, r_shift: Int): Vector[RatingLine] = {
    u.interests.get.getData.map(_._2).zipWithIndex.map({case(rr, i) => new RatingLine(u.vkUser.vkId, i + 1, rr + r_shift)})
  }

  def toRatingsList(dbUsers: Iterator[DBUser], r_shift: Int): Vector[RatingLine] = {
      dbUsers.toVector.flatMap(mkRatings(_, r_shift))
  }


  val fw = new FileWriter("/Users/buzun/gb/data/ratings.txt")

  toRatingsList(new UsersModel().getAllFastWithInterests, 1).foreach(t => fw.write(t.toString + "\n"))

  fw.close()
}
