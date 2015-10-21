package textproc

/**
 * Created by buzun on 09/08/15.
 */
object TrashFilter {

  val r1: Set[String] = io.Source.fromFile("/Users/buzun/gb/data/stopwords/stop-words-russian.txt").getLines().map(_.trim).toSet
  val r2: Set[String] = io.Source.fromFile("/Users/buzun/gb/data/stopwords/stop-words-english1.txt").getLines().map(_.trim).toSet
  val r3: Set[String] = io.Source.fromFile("/Users/buzun/gb/data/stopwords/stop-words-english2.txt").getLines().map(_.trim).toSet
  val r4: Set[String] = io.Source.fromFile("/Users/buzun/gb/data/stopwords/vk1.txt").getLines().map(_.trim).toSet
  val r5: Set[String] = io.Source.fromFile("/Users/buzun/gb/data/stopwords/vk2.txt").getLines().map(_.trim).toSet

  val src: Set[String] = r1 ++ r2 ++ r3 ++ r4 ++ r5

  def apply(vector: IndexedSeq[String]): IndexedSeq[String] = vector.filter(s => !src.contains(s))

}
