package load

import java.io._
import db.Interests
import org.apache.poi.ss.util._
import org.apache.poi.xssf.usermodel.XSSFWorkbook


class GoogleTable(val path: String = "/Users/buzun/Downloads/data4.xlsx") {

  val wb = new XSSFWorkbook(path)

  val sheet = wb.getSheetAt(0)
  val cols: Int = sheet.getRow(0).getLastCellNum
  val rows: Int = sheet.getLastRowNum + 1
  val header = sheet.getRow(0)

  val headerRow = Vector.range(0, cols).map(i => header.getCell(i).toString.trim.toLowerCase)

  private val namesSet: Map[String, Int] = headerRow.zipWithIndex.toMap
  val interestsNames = namesSet.filter(_._1.startsWith("мне интересны"))
  val interestsColumns = interestsNames.values.toVector

  val vkIdCellNum: Int = namesSet.find(_._1.startsWith("напишите id")).get._2

  def get(i: Int, j: Int) = sheet.getRow(i).getCell(j).toString

  def getVkId(row: Int): String = {
    assert(row > 0 & row < rows)

      sheet.getRow(row).getCell(vkIdCellNum).toString

  }

  def getVkIdasInt(row: Int): Int = {
    assert(row > 0 & row < rows)

    sheet.getRow(row).getCell(vkIdCellNum).toString.toDouble.toInt

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

  val collectionsCellNum: Int = namesSet.find(_._1.startsWith("я коллекционирую")).get._2
  val collectionsNames = Vector.range(1, cols).flatMap(i => sheet.getRow(i).getCell(collectionsCellNum).toString.trim.split(",")).zipWithIndex.toMap

  def filerInterests(row: Int): Interests = {
    new Interests(
    interestsColumns.map(col => get(row, col)).map(numberByInterestPhr)
    )
  }

  def vkId2Interests: Map[Int, Interests] = {
    Vector.range(1, rows).map(r => (getVkIdasInt(r), filerInterests(r))).toMap
  }

}
