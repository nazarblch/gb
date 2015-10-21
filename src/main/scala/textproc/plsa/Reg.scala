package textproc.plsa

import breeze.collection.mutable.SparseArray
import breeze.linalg.DenseVector


class SupervisedTopic(dist: SparseArray[Double], wIds: Vector[Int]) extends Topic(dist) {

  val tau = 10.0

  override def newFromNw(topics: Vector[Topic], sumnw: DenseVector[Double] = null): Topic = {
    wIds.foreach(i => {
      addNw(i, -5)
    })
    nw.keysIterator.foreach(i => {
      addNw(i, tau)
    })
    new SupervisedTopic(super.newFromNw(topics).dist, wIds)
  }

}


abstract class DocReg(decoratedDoc: Document) extends Document(decoratedDoc.ws, decoratedDoc.dist) {
  override def updateFromNt(topics: Vector[Topic]): Unit = decoratedDoc.updateFromNt(topics)
}

class SupervisedDocument(doc: Document, tIds: Vector[Int]) extends DocReg(doc) {

  val tau = 1.0

  override def updateFromNt(topics: Vector[Topic]): Unit = {
    doc.nt.indices.foreach(i => {
      doc.nt(i) += tau
    })
    super.updateFromNt(topics)
  }

}

trait SmoothReg extends Topic {

  val beta = 5.0

  abstract override def newFromNw(topics: Vector[Topic], sumnw: DenseVector[Double] = null): Topic = {
    Range(0, Topic.WCOUNT).foreach(i => {
      addNw(i,beta)
    })
     new Topic(super.newFromNw(topics).dist) with SmoothReg
  }
}


trait SparceReg extends Topic {

  val beta = -5.0

  abstract override def newFromNw(topics: Vector[Topic], sumnw: DenseVector[Double] = null): Topic = {
    nw.keysIterator.foreach(i => {
      addNw(i,beta)
    })
    new Topic(super.newFromNw(topics).dist) with SparceReg
  }
}

trait AntiCorReg extends Topic {

  val tau = -0.5

  abstract override def newFromNw(topics: Vector[Topic], sumnw: DenseVector[Double]): Topic = {

    nw.keysIterator.foreach(i => {
      nw(i) += tau * dist(i) * (sumnw(i) - dist(i))
    })
    new Topic(super.newFromNw(topics).dist) with AntiCorReg
  }
}



trait DocSparceReg extends Document {

  val beta = -0.5
  val tau = -0.1

  abstract override def updateFromNt(topics: Vector[Topic]): Unit = {

    nt.indices.foreach(i => {
      nt(i) += beta + tau * (size / topics(i).nwsum) * dist(i)
    })
    super.updateFromNt(topics)
  }
}
