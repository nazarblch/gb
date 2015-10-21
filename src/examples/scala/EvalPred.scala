/**
 * Created by nazar on 09/06/15.
 */
object EvalPred extends App {

  val err = io.Source.fromFile("/Users/buzun/gb/librec/Results/PMF-rating-predictions fold [1].txt").getLines().drop(1).map(line => {
    val r1 = line.split(" ")(2).trim.toDouble.floor
    val r2 = line.split(" ")(3).trim.toDouble.round

//    val br1: Int = if (r1 > 2.6) 1 else 0
//    val br2: Int = if (r2 > 2.6) 1 else 0

    math.abs(r1 - r2)
  }).toVector

  println(err.sum.toDouble / err.length)

}
