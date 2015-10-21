import java.io.FileWriter

import db.UsersModel
import load.IdVkReader
import textproc.Tokenizer
import util.WriteSeq

import scala.io.Source

object Text2ClVec extends App {

  val w2cl: Map[String, Int] = io.Source.fromFile("/Users/buzun/trunk/w2cl.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap

  val dim  = w2cl.values.max

  def phr2cls(phr: String): Vector[Int] = {
    phr.split(" ").toVector.map(w => {
      if (w2cl.get(w).isEmpty) println(w)
      w2cl.getOrElse(w, -1)
    }).filter(_ != -1)
  }



//  UsersModel.getAll.foreach(u => {
//    val cls =  phr2cls(u.vkUser.text)
//    UsersModel.addNumericText(u, cls)
//  })


  WriteSeq( w2cl.groupBy(_._2).mapValues(v => v.keys).toVector.sortBy(_._1).map({case(k,v) => k + "|" +  v.mkString(" ")}), "/Users/buzun/gb/data/cl.txt")


}
