import load.{IdVkReader, WishesReader, VectorUsersReader}
import util.WriteSeq

/**
 * Created by nazar on 6/26/15.
 */
object TrustEdges extends App {

  val users = IdVkReader.toId(VectorUsersReader.load)

  val u2w_filtered = WishesReader.filterUserWish(WishesReader.u2w, 20, 50, 220000)

  val filt_users =  users.filter(u => u2w_filtered.contains(u.id))

  println("filtered ", filt_users.size)

  var c = 0
  val edges = filt_users.flatMap(u => {
    c += 1
    println(c)
    u.findFriends(filt_users).map(o => u.id + " " + o.id + " 1")
  })

  WriteSeq.apply(edges, "/home/nazar/gb/data/trust.txt")


}
