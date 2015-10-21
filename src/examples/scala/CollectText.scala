import java.io.FileWriter

import db.{UsersModel, GroupsModel}
import textproc.Tokenizer
import util.WriteSeq


object CollectText extends App {

  def getText(path: String, n: Int = 1000): IndexedSeq[String] = {
    io.Source.fromFile(path).getLines().take(n).toVector
  }

  def getGroupsText: IndexedSeq[String] = {
     GroupsModel.getAll.map(g => g.name + "\n" + g.text).toVector
  }

  def getWallsText: IndexedSeq[String] = {
    new UsersModel().getAllFast.map(u => u.vkUser.text).toVector
  }

  def getWishesText(path: String, n: Int = 1000): IndexedSeq[String] = {
    io.Source.fromFile(path).getLines().take(n)
      .map(line => {
      if (line.split(",").length > 1)
        Some(line.split(",")(1))
      else None
    }).filter(_.isDefined).map(_.get).toVector
  }


  val text = (getGroupsText ++ getWallsText).filter(_.trim.length > 100)
  val fw1 = new FileWriter("/Users/buzun/trunk/text_mongo.txt")
  text.foreach(s => fw1.write(s + "\n"))


}
