package util

/**
 * Created by buzun on 07/08/15.
 */
object MapMerger {

  def apply[T1, T2](m1: Map[Int, T1], m2: Map[Int, T2]): Map[Int, (T1, Option[T2])] = {
    m1.keySet.map(i => i -> (m1.get(i).get, m2.get(i))).toMap
  }
}
