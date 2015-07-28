import java.io.FileWriter

import load.IdVkReader
import user.MetricUser

object UserData extends App {


  val ids: Set[Int] = IdVkReader.activeIds



  val f = new FileWriter("/home/nazar/gb/data/adddata.txt")

  io.Source.fromFile("/home/nazar/gb/data/User2ClVec.txt").getLines().toVector.map(l => {
    (l.split(" ")(0).toInt, l.split(" ")(1))
  }).map({case (id, v) => (IdVkReader.vk2id.get(id).get, v)}).filter(p => ids.contains(p._1)).sortBy(_._1).foreach(p => {
    f.write(p._2 + "\n")
  })

  f.close()


}
