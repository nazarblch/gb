package user

import textproc.{Text2VecSeq, Tokenizer}


class User(val id: Int, val wallText: String, val gropsText: Seq[Group]) {

  val tok = new Tokenizer

  def getText: String = tok.removeTrash(gropsText.map(_.getShortText(30)).mkString(" . ") + " . " + wallText).split(" ").
    filter(_.trim.length > 1).mkString(" ")
}

class MetricUser(val id: Int, val textCoordinates: IndexedSeq[Array[Double]]) {
  override def toString = id + " " + textCoordinates.map(_.mkString(",")).mkString(";")

  def setId(newId: Int) = new MetricUser(newId, textCoordinates)
}

class VectorUser(val id: Int, val coord: Vector[Double]) {

  val norm: Double = coord.sum

  def sim(o: VectorUser): Double = {
    coord.zip(o.coord).map(p => p._1 * p._2).sum / (norm * o.norm)
  }

  class Ord(val set: collection.mutable.Set[VectorUser]) {
    def + (u: VectorUser): Ord = {
      set.add(u)
      this
    }
    def - (u: VectorUser): Ord = {
      set.remove(u)
      this
    }
    def minElem: VectorUser = set.minBy(sim)
    def size = set.size
  }

  def maxes(n:Int)(l:Traversable[VectorUser]) =
    l.foldLeft(new Ord(collection.mutable.Set())) { (xs,y) =>
      if (xs.size < n) xs + y
      else {
        val first = xs.minElem
        if (sim(first) < sim(y)) xs - first + y
        else xs
      }
    }

  def findFriends(seq: IndexedSeq[VectorUser]): IndexedSeq[VectorUser] = {
    maxes(20)(seq).set.toVector
  }
}

object MetricUser {

  def apply(u: User): MetricUser = {
    val data = Text2VecSeq.apply(u.getText)
    new MetricUser(u.id, data)
  }

  def apply(s: String): MetricUser = {
    val id = s.split(" ")(0).toInt
    val data = s.split(" ")(1).split(";").map(_.trim.split(",").map(_.toDouble))
    new MetricUser(id, data)
  }
}


object VectorUser {

  def apply(s: String): VectorUser = {
    new VectorUser(s.split(" ")(0).trim.toInt, s.split(" ")(1).trim.split(",").map(_.toDouble).toVector)
  }
}




