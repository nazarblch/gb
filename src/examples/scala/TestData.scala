import load.{WishesReader, UserById}
import textproc.Text2VecSeq

/**
 * Created by nazar on 7/2/15.
 */
object TestData extends App {

  //val data = io.Source.fromFile("/home/nazar/gb/data/data.txt").getLines().take(2).toVector.last

  //data.split(";").map(s => s.split(",").map(_.toDouble)).map(v => Text2VecSeq.findWord(v)).foreach(println)

  //println(UserById.get(178).split(" ").foreach(println))

  val labels = io.Source.fromFile("/home/nazar/gb/data/labels.txt").getLines().flatMap(_.split(",").map(_.toInt)).toVector

  println(labels.sum.toDouble / 5000)



  //println(WishesReader.u2w.get(178).mkString(" "))
}
