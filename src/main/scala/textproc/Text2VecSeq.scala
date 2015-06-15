package textproc


object Text2VecSeq {

  val w2v = new Word2Vec("/home/nazar/trunk/w2v.txt")

  def apply(txt: String): IndexedSeq[Array[Double]] = {
    val tok = new Tokenizer
    w2v(tok(txt))
  }

}
