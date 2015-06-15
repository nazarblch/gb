import java.io.FileWriter

/**
 * Created by nazar on 5/31/15.
 */
object ToBinary extends App {

  val fw = new FileWriter("/home/nazar/librec/librec/demo/Datasets/FilmTrust/ratings_bin.txt")

  io.Source.fromFile("/home/nazar/librec/librec/demo/Datasets/FilmTrust/ratings.txt").getLines()
  .foreach(line => {
    val u = line.split(" ")(0)
    val i = line.split(" ")(1)
    val r = line.split(" ")(2).toDouble
    val br = if (r > 3) "1" else "0"
    fw.write(u + " " + i + " " + br + "\n")
  })

  fw.close()

}
