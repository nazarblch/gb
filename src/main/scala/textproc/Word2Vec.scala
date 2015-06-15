package textproc

class Word2Vec(vectorsPath: String) {

  val w2vec = io.Source.fromFile(vectorsPath).getLines().map(line => {
    val arr = line.split(" ")
    val w = arr(0)
    val vec = arr.drop(1).map(_.toDouble)
    (w, vec)
  }).toMap

  def apply(s: String): Option[Array[Double]] = {
    w2vec.get(s.trim)
  }

  def apply(snt: IndexedSeq[String]): IndexedSeq[Array[Double]] = {
    snt.map(apply).filter(_.isDefined).map(_.get)
  }

}
