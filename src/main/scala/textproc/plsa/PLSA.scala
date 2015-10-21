package textproc.plsa

import java.util.Random

import breeze.collection.mutable.SparseArray
import breeze.linalg.{SparseVector, DenseVector}
import breeze.stats.distributions.Dirichlet
import db.{VKGroup, TopicVKGroup}
import textproc.Dictionary


/**
 * Created by buzun on 13/08/15.
 */


class Document(val ws: Vector[Word], val dist: Array[Double]) {

  val nt: Array[Double] = Array.fill(dist.length)(0.0)

  val size: Int = ws.map(_.count).sum

  def updateFromNt(topics: Vector[Topic]): Unit = {
    dist.indices.foreach(i => {
      nt(i) = if (nt(i) > 1e-5) nt(i) else 0
    })
    val norm = nt.sum
    dist.indices.foreach(i => {
      dist(i) = nt(i) / norm
      nt(i) = 0.0
    })
  }

  def calcNwt(topics: Vector[Topic]): Unit = {
    ws.foreach(w => {

      val ftprod = topics.indices.filter(dist.apply(_) > 0.001).filter(i => topics(i).contains(w.id)).map(tid => {
        (w, tid, dist(tid) * topics(tid).dist(w.id))
      }).filter(_._3 > 0.00001)

      val norm = ftprod.map(_._3).sum

      ftprod.foreach({ case (u, tid, ft) =>
        val nwt = u.count * ft / norm
        nt(tid) += nwt
        topics(tid).addNw(w.id, nwt)
      })

    })

    updateFromNt(topics)
  }

}

object Document {

  val TCOUNT: Int = 61

  val r = Dirichlet(Array.fill(TCOUNT)(0.2))

  def apply(seq: Vector[String]): Document = {
    val data = seq.map(s => Dictionary.str2word.get(s)).filter(_.isDefined).map(_.get.id)
    new Document(data.groupBy(x => x).mapValues(_.size).toVector.map({case (id, c) => new Word(id, c)}), r.draw().toArray) with DocSparceReg
  }

  def apply(g: VKGroup): Document = {
    apply(g.name.split(" ").toVector ++ g.text.split(" ").toVector.take(500))
  }
}

class Topic(val dist: SparseArray[Double]) {

  // todo: vector to map

  protected val nw: SparseArray[Double] = SparseArray.create[Double](Topic.WCOUNT)()
  var nwsum: Double = 0.0

  def contains(w: Int) = {
    dist.contains(w)
  }

  def addNw(w: Int, c: Double): Unit = {
    nw(w) += c
    nwsum += c
  }

  def newFromNw(topics: Vector[Topic], sumnw: DenseVector[Double] = null): Topic = {
    nw.keysIterator.foreach(i => {
      nw(i) =  if (nw(i) > 1e-5) nw(i) else 0
    })
    val norm = nw.valuesIterator.sum
    new Topic(nw.map(_ / norm))
  }

  def mfWords: Vector[String] = dist.iterator.toVector.sortBy(-_._2).map(_._1).take(40).map(Dictionary.id2str)

}

object Topic {

  val WCOUNT = Dictionary.str2word.size

  val r = Dirichlet(Array.fill(WCOUNT)(0.01))

  def apply(ws: Vector[String]): SupervisedTopic = {
    val ids = ws.map(s => Dictionary.str2word(s).id)
    val t = apply()
    new SupervisedTopic(t.dist, ids)
  }

  def apply(): Topic = {

    val arr: SparseArray[Double] = SparseArray.create[Double](WCOUNT)()
    val gen = r.draw()

    Range(0, WCOUNT).foreach(i => {
      if (gen(i) > 0.0001) arr(i) = gen(i)
    })

    new Topic(arr) with SparceReg
  }

  def data(): SparseArray[Double] = {

    val arr: SparseArray[Double] = SparseArray.create[Double](WCOUNT)()
    val gen = r.draw()

    Range(0, WCOUNT).foreach(i => {
      if (gen(i) > 0.0001) arr(i) = gen(i)
    })

    arr
  }

}


class Word(val id: Int, val count: Int) {
}

class PLSA(val init: Vector[Topic]) {


  def train(docs: Seq[Document], iters: Int): Vector[Topic] = {

    var tmp_topics = init

    for(i <- 0 to iters - 1) {
      println("iter: " + i)
      tmp_topics = mkIter(docs, tmp_topics)
    }

    tmp_topics
  }

  def mkIter(docs: Seq[Document], topics: Vector[Topic]): Vector[Topic] = {
    docs.foreach(_.calcNwt(topics))
    val sumdist: DenseVector[Double] = topics.map(t => DenseVector(t.dist.toArray)).reduce(_+_)
    topics.map(_.newFromNw(topics, sumdist))
  }

}
