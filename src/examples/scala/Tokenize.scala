import textproc.{Text2VecSeq, Word2Vec, Tokenizer}

object Tokenize extends App {

  val vecs = Text2VecSeq.apply("Трансляции матчей Чемпионата,Мира по футболу 2014 из Бразилии на 3 метровом проекторе+++")

  vecs.foreach(a => println(a.mkString(",")))
}
