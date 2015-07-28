package textproc


object Text2VecSeq {

  val w2v = new Word2Vec("/home/nazar/trunk/w2vn20.txt")

  def apply(txt: String): IndexedSeq[Array[Double]] = {
    val tok = new Tokenizer
    w2v(tok(txt))
  }

  def findWord(v: Array[Double]): String = {
    w2v.w2vec.minBy(_._2.zip(v).map(p => math.abs(p._1 - p._2)).sum)._1
  }

}
