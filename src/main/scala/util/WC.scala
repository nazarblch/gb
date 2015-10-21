package util

/**
 * Created by buzun on 08/08/15.
 */
object WC extends App {

  def w2count(data: Vector[String]): Map[String, Int] = data.foldLeft(Map.empty[String, Int]){
    (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
  }

  val text = io.Source.fromFile("/Users/buzun/trunk/text_mongo.txt").getLines().flatMap(_.split(" ")).toVector.map(_.trim)

  WriteSeq(w2count(text).toVector.map(p => p._1 + " " + p._2), "/Users/buzun/trunk/wc.txt")

}
