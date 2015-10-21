package textproc

import util.MapMerger


class DictWord(val id: Int, val count: Int, val clusterId: Int) {}


object Dictionary {

  val toc = new Tokenizer

  val w2cl: Map[String, Int] = io.Source.fromFile("/Users/buzun/trunk/w2cl.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap

  val wc: Map[String, Int] = io.Source.fromFile("/Users/buzun/trunk/wc.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap

  val topics: Map[String, Vector[String]] = io.Source.fromFile("/Users/buzun/gb/data/topics.txt").getLines()
    .map(line => (line.split(":")(0), Vector(toc.stem(line.split(":")(0))) ++ line.split(":")(1).split(",").toVector)).toMap

  val str2word: Map[String, DictWord] = MapMerger(wc, w2cl).filter(_._2._2.isDefined).toVector.zipWithIndex.map({case((s, (c, cl)),id) =>
    (s, new DictWord(id, c, cl.get))
  }).toMap

  val id2str : Map[Int, String] = str2word.map({case (s, dw) => (dw.id, s)})

}
