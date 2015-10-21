package load

import java.io._
import db.{InterestsModel, Interests}
import org.apache.poi.ss.util._
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import textproc.Tokenizer


class GoogleTable(val path: String = "/Users/buzun/Downloads/data4.xlsx") {

  val wb = new XSSFWorkbook(path)

  private val sheet = wb.getSheetAt(0)
  val cols: Int = sheet.getRow(0).getLastCellNum
  val rows: Int = sheet.getLastRowNum + 1
  val header = sheet.getRow(0)

  val headerRow = Vector.range(0, cols).map(i => header.getCell(i).toString.trim.toLowerCase)

  private val namesSet: Map[String, Int] = headerRow.zipWithIndex.toMap
  val interestsNames = namesSet.filter(_._1.startsWith("мне интересны"))
  val interestsColumns = interestsNames.values.toVector.sorted
  val col2name = interestsNames.map(_.swap)


  private val vkIdCellNum: Int = namesSet.find(_._1.startsWith("напишите id")).get._2

  def get(i: Int, j: Int) = sheet.getRow(i).getCell(j).toString

  def getVkId(row: Int): String = {
    assert(row > 0 & row < rows)

      sheet.getRow(row).getCell(vkIdCellNum).toString

  }

  def getVkIdasInt(row: Int): Int = {
    assert(row > 0 & row < rows)

    sheet.getRow(row).getCell(vkIdCellNum).toString.toDouble.toInt

  }

  def getInterestName(id: Int): String = {
    val c = interestsColumns(id)
    col2name.get(c).get
  }

  def numberByInterestPhr(s: String): Int = {
    if (s.toLowerCase.trim equals "да") 2
    else if (s.toLowerCase.trim startsWith "скорее да") 1
    else if ((s.toLowerCase.trim equals "затрудняюсь ответить") || s.trim.length == 0) 0
    else if (s.toLowerCase.trim startsWith "скорее нет") -1
    else if (s.toLowerCase.trim equals "нет") -2
    else {
      throw new Exception("number not found: " + s)
    }
  }

  def save(): Unit = {
    val stream = new FileOutputStream(path)
    wb.write(stream)
    stream.close()
  }

  def updateId(row: Int, id: Int) {
    sheet.getRow(row).getCell(vkIdCellNum).setCellValue(id.toString)
    save()
  }

  def deleteRow(row: Int): Unit = {
    sheet.removeRow(sheet.getRow(row))

  }

  def findId(subStr: String): Int = {
    val res = interestsNames.find(_._1.contains(subStr))
    assert(res.size == 1)
    res.head._2
  }

  val collectionsCellNum: Int = namesSet.find(_._1.startsWith("я коллекционирую")).get._2
  val collectionsNames = Vector.range(1, cols).flatMap(i => sheet.getRow(i).getCell(collectionsCellNum).toString.trim.split(",")).zipWithIndex.toMap

  def filerInterests(row: Int): Interests = {
    new Interests(
      interestsNames.toVector.map({case (name, col) =>
        val title  = getTitle(name)
        val score = numberByInterestPhr(get(row, col))
        (InterestsModel.title2interest(title), score)
      }).sortBy(_._1.id)
    )
  }

  def vkId2Interests: Map[Int, Interests] = {
    Vector.range(1, rows).map(r => (getVkIdasInt(r), filerInterests(r))).toMap
  }

  val cat2str = Map(
    0 -> "спорта",
    1 -> "экстрима",
    2 -> "танцев",
    3 -> "активные развлечения",
    4 -> "рукоделия",
    5 -> "музыкальные развлечение",
    6 -> "спокойные развлечения"
  )

  def getCat(name: String): Int = {
    val res = cat2str.find(p => p._2.r.findAllIn(name.replaceAll(":", " ")).nonEmpty )
    if (res.isEmpty) println(name.replaceAll(":", " "))
    res.get._1
  }

  def getTitle(name: String): String = {
    name.split("\\[")(1).split("\\]")(0).trim
  }


}


class TopicsTable(val path: String = "/Users/buzun/Downloads/key_words.xlsx") {

  val wb = new XSSFWorkbook(path)

  val tok = new Tokenizer()

  private val sheet = wb.getSheetAt(0)
  val cols: Int = sheet.getRow(1).getLastCellNum
  val header = sheet.getRow(1)
  val rows: Int = sheet.getLastRowNum + 1

  val headerRow = Vector.range(0, cols).map(i => header.getCell(i).toString.trim.toLowerCase)
  val activeCols = headerRow.zipWithIndex.filter(_._1.length > 1).map(_._2)

  def getWords(col: Int): Set[String] = {
    Vector.range(1, rows).map(r => sheet.getRow(r).getCell(col).toString)
      .map(s => tok(s).mkString(" ").trim).filter(_.length > 0).toSet
  }

}


class TopicsVKGroupsTable(val path: String = "/Users/buzun/Downloads/topic_groups.xlsx") {

  val wb = new XSSFWorkbook(path)

  val tok = new Tokenizer()

  private val sheet = wb.getSheetAt(0)
  val cols: Int = sheet.getRow(1).getLastCellNum
  val header = sheet.getRow(1)
  val rows: Int = sheet.getLastRowNum + 1

  val headerRow = Vector.range(0, cols).map(i => header.getCell(i).toString.trim.toLowerCase)
  val activeCols = headerRow.zipWithIndex.filter(_._1.length > 1).map(_._2)

  def getWords(col: Int): Set[String] = {
    Vector.range(2, rows).map(r => sheet.getRow(r).getCell(col).toString)
      .map(s => s.trim).filter(_.length > 0).toSet
  }

  def getWordsWithHeader(col: Int): (String, Set[String]) = {
    (headerRow(col), getWords(col))
  }



}

