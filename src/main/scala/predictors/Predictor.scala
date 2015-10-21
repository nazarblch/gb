package predictors

import db.{DBInterest, VKUser}


class RankedInterests(private val data: Vector[(DBInterest, Double)]) {
  // assert(math.abs(data.map(_._2).sum - 1) < 0.01)

  def getTop(n: Int): Vector[(DBInterest, Double)] = data.sortBy(-_._2)
  def getData: Vector[(Int, Double)] = data.map({case(k,v) => (k.id, v)})
}


trait Predictor {

  def rankInterests(users: IndexedSeq[VKUser]): IndexedSeq[(VKUser, RankedInterests)]

}
