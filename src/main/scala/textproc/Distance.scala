package textproc

import sys.process._

object Distance {

  val out = new StringBuilder
  val err = new StringBuilder

  val logger = ProcessLogger(
    (o: String) => out.append(o),
    (e: String) => err.append(e))

  def exec(word: String) = {
    Process("/Users/buzun/trunk/distance /Users/buzun/trunk/vectors.bin " + word) ! logger
    val res = out.result().split(" ").map(_.trim)
    out.clear()
    res
  }

}
