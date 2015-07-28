import java.io.FileWriter

import load.WishesReader

/**
 * Created by nazar on 7/9/15.
 */
object CollectWishes extends App {

  val u2w_filtered = WishesReader.filterUserWish(WishesReader.u2w, 10, 30, 220000)

  println(u2w_filtered.size)
  val wids = u2w_filtered.flatMap(_._2).toSet
  println(wids.size)

  val fw = new FileWriter("/home/nazar/gb/data/activeWishes.txt")

  WishesReader.wishes.filter(p => wids.contains(p._1)).foreach({case (i, w) => fw.write(w.toString + "\n")})

  fw.close()

}
