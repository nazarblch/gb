import java.io.FileWriter

import textproc.Tokenizer


object CollectText extends App {

  def getText(path: String, n: Int = 1000): IndexedSeq[String] = {
    io.Source.fromFile(path).getLines().take(n).toVector
  }

  def getGroupsText(path: String, n: Int = 1000): IndexedSeq[String] = {
    io.Source.fromFile(path).getLines().take(n)
      .map(line => {
        if (line.split("\\|").length > 2)
        Some(line.split("\\|")(1) + " " + line.split("\\|")(2))
        else None
    }).filter(_.isDefined).map(_.get).toVector
  }

  def getWishesText(path: String, n: Int = 1000): IndexedSeq[String] = {
    io.Source.fromFile(path).getLines().take(n)
      .map(line => {
      if (line.split(",").length > 1)
        Some(line.split(",")(1))
      else None
    }).filter(_.isDefined).map(_.get).toVector
  }

  def getWallsText(path: String, n: Int = 1000): IndexedSeq[String] = {
    io.Source.fromFile(path).getLines().take(n)
      .map(line => {
      if (line.split("\\|").length > 3)
        Some(line.split("\\|")(3))
      else None
    }).filter(_.isDefined).map(_.get).toVector
  }


  val nn = 100000


  val text = // getText("/home/nazar/trunk/text8", nn) ++
             getText("/home/nazar/trunk/text1", nn)++
             getGroupsText("/home/nazar/Downloads/v0/groups.csv", nn) ++
             getWallsText("/home/nazar/Downloads/v0/walls.csv", nn) ++
             getWishesText("/home/nazar/Downloads/v0/wishes.csv", nn)



  val toc = new Tokenizer
  val toc_text = toc(text)

  val fw = new FileWriter("/home/nazar/trunk/text_all")

  fw.write(toc_text.mkString(" "))

  fw.close()

}
