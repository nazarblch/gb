/**
 * Created by nazar on 09/06/15.
 */
object EvalPred extends App {

  val err = io.Source.fromFile("/home/nazar/gb/librec/Results/SocialMF-rating-predictions fold [1].txt").getLines().drop(1).map(line => {
    val r1 = line.split(" ")(2).trim.toDouble
    val r2 = line.split(" ")(3).trim.toDouble

    val br1: Int = if (r1 > 2.6) 1 else 0
    val br2: Int = if (r2 > 2.6) 1 else 0

    math.abs(br1 - br2)
  }).toVector

  println(err.sum.toDouble / err.length)

}
