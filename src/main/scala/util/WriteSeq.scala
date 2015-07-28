package util

import java.io.FileWriter

/**
 * Created by nazar on 6/26/15.
 */
object WriteSeq {
  def apply(seq: IndexedSeq[Any], path: String): Unit = {
    val fw1 = new FileWriter(path)

    seq.foreach(s => fw1.write(s.toString + "\n"))

    fw1.close()
  }
}
