package textproc

import java.io.FileInputStream

import opennlp.tools.sentdetect.{SentenceDetectorME, SentenceModel}
import opennlp.tools.stemmer.Stemmer
import opennlp.tools.tokenize.{TokenizerME, TokenizerModel}
import org.tartarus.snowball.ext.RussianStemmer
import org.tartarus.snowball.ext.EnglishStemmer


class Tokenizer {

  def findSent(s: String): Vector[String] = {
    val modelIn = new FileInputStream("data/en-sent.bin")
    val model = new SentenceModel(modelIn)
    modelIn.close()
    val sentenceDetector = new SentenceDetectorME(model)
    val sentences = sentenceDetector.sentDetect(s)

    sentences.toVector

  }

  def findWords(s: String): Vector[String] = {
//    val modelIn = new FileInputStream("data/en-token.bin")
//    val model = new TokenizerModel(modelIn)
//    modelIn.close()
//    val tokenizer = new TokenizerME(model)
//
//    val tokens = tokenizer.tokenize(s)
//    tokens.toVector
    s.split(" ").filter(_.trim.length > 0).toVector

  }

  def removeTrash(s: String): String = {
    s.replaceAll("[^a-zA-Zа-яА-Я0-9]", " ")
  }

  def stem(s: String): String = {
    val stemmers = Array(new RussianStemmer, new EnglishStemmer)
    stemmers.foldLeft(s)((res, st) => {
      st.setCurrent(res)
      st.stem()
      st.getCurrent
    })
  }

  def apply(s: String): Vector[String] = {
    findWords(removeTrash(s)).map(_.toLowerCase).map(stem).filter(_.length > 1)
  }



}
