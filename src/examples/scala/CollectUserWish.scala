import java.io.FileWriter
import java.util.Random

import load.{WishesReader, IdVkReader}
import user.Wish

object CollectUserWish extends App {



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


  val u2w_filtered = WishesReader.filterUsersWithText( WishesReader.filterUserWish(WishesReader.u2w, 10, 100, 220000) )

  println(u2w_filtered.size)

  val fw = new FileWriter("/home/nazar/gb/data/ratings.txt")

  toRatingsList(u2w_filtered, 1).foreach(t => fw.write(t._1 + " " + t._2 + " " + t._3 + "\n"))

  fw.close()
}
