package user

import textproc.{Text2VecSeq, Tokenizer}


class User(val id: Int, val wallText: String, val gropsText: Seq[Group]) {
  def getText: String = wallText + ". " + gropsText.map(g => g.name + ". " + g.text).mkString(". ")
}

class MetricUser(val id: Int, val textCoordinates: IndexedSeq[Array[Double]]) {
}

object MetricUser {

  def apply(u: User): MetricUser = {
    val data = Text2VecSeq.apply(u.getText)
    new MetricUser(u.id, data)
  }
}
