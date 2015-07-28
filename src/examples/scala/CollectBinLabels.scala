import java.io.FileWriter

import load.{IdVkReader, WishesReader}

/**
 * Created by nazar on 6/30/15.
 */
object CollectBinLabels extends App {

  val u2w_filtered = WishesReader.filterUsersWithText( WishesReader.filterUserWish(WishesReader.u2w, 10, 50, 220000) )

  println(u2w_filtered.size)

  val w2index = u2w_filtered.toVector.flatMap(_._2).toSet.zipWithIndex.toMap
  val wsize = u2w_filtered.toVector.flatMap(_._2).distinct.size

  println(wsize)

  def w2vec(ws: Vector[Int]): Vector[Int] = {
    val res = Array.fill(wsize)(0)
    ws.foreach(w => res(w2index(w)) = 1)
    res.toVector
  }

  val fw = new FileWriter("/home/nazar/gb/data/u2labels.txt")

  u2w_filtered.foreach({case (id, ws) =>
     fw.write(id + " " + w2vec(ws).mkString(",") + "\n")
  })

  fw.close()
}
