import java.io.FileWriter

import load.IdVkReader
import textproc.Tokenizer

import scala.io.Source

object Text2ClVec extends App {

  val w2cl: Map[String, Int] = io.Source.fromFile("/home/nazar/trunk/w2cl.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap

  val cl2c = w2cl.values.groupBy(x => x).map(p => (p._1, p._2.size)).toMap
  val dim  = cl2c.size

  val toc = new Tokenizer

  def phr2cls(phr: String): Vector[Int] = {
    toc.removeTrash(phr).split(" ").toVector.map(_.toLowerCase.trim).map(toc.stem).map(w2cl.getOrElse(_, -1))
      .filter(_ != -1)
  }

  def cls2vec(cls: Vector[Int]): Vector[Double] = {
    val v = Array.fill[Double](dim)(0.0)
    cls.groupBy(x => x).toVector.foreach(p => v(p._1) = p._2.size.toDouble / cl2c.get(p._1).get)
    val norm = v.sum
    v.map(_ / norm).toVector
  }

  val u2cls: Vector[(Int, Vector[Double])] = io.Source.fromFile("/home/nazar/gb/data/Users.txt").getLines()
    .filter(l => IdVkReader.activeVkIds.contains(l.split(" ")(0).toInt))
    .toVector
    .map(line => (line.split(" ")(0).toInt, cls2vec(phr2cls(line.split(" ").drop(1).mkString(" ")))))


  val fw = new FileWriter("/home/nazar/gb/data/User2ClVec.txt")

  u2cls.foreach({case(id, vec) =>
      fw.write(id + " " + vec.mkString(",") + "\n")
  })

  fw.close()


}
