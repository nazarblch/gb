import java.io.FileWriter

import load.IdVkReader
import user.MetricUser

/**
 * Created by nazar on 6/30/15.
 */
object SyncData extends App {

  val labels = io.Source.fromFile("/home/nazar/gb/data/u2labels.txt").getLines().map(_.split(" ")(0).toInt).toVector
  val lset = labels.toSet

  val data = io.Source.fromFile("/home/nazar/gb/data/metricUsers.txt").getLines()
    .filter(l => lset.contains( IdVkReader.vk2id.get(l.split(" ")(0).toInt).get )).map(line => {
    MetricUser(line)
  }).toVector.map(u => u.setId(IdVkReader.vk2id.get(u.id).get)).sortBy(_.id)

  println(data.size)

  val uidsset = data.map(_.id).toSet

  println(data.map(_.id).mkString(" "))

  val fw = new FileWriter("/home/nazar/gb/data/data.txt")

  data.foreach(u => {
    fw.write(u.textCoordinates.map(_.mkString(",")).mkString(";") + "\n")
  })

  fw.close()


  val f = new FileWriter("/home/nazar/gb/data/labels.txt")

  io.Source.fromFile("/home/nazar/gb/data/u2labels.txt").getLines().toVector.map(l => {
    (l.split(" ")(0).toInt, l.split(" ")(1))
  }).filter(p => uidsset.contains(p._1)).sortBy(_._1).foreach(p => {
    f.write(p._2 + "\n")
  })

  f.close()

  val f1 = new FileWriter("/home/nazar/gb/data/ids.txt")

  uidsset.foreach(i => {
    f1.write(i + " " + IdVkReader.id2vk.get(i).get + "\n")
  })

  f1.close()


}
