import java.io.FileWriter

import db.UsersModel
import load.GoogleTable
import weka.FeaturesExtractor
import weka.core.converters.ConverterUtils.DataSource

import scala.collection.mutable.ArrayBuffer

/**
 * Created by buzun on 14/08/15.
 */
object ExtractFeatures extends App {

  val w2cl: Map[String, Int] = io.Source.fromFile("/Users/buzun/trunk/w2cl.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap

  val wc: Map[String, Int] = io.Source.fromFile("/Users/buzun/trunk/wc.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap


  val usersIdsBuf: ArrayBuffer[Int] = ArrayBuffer()

  val p = 2000

  val table = new GoogleTable()

  def mkArff(): Unit = {

    val fw = new FileWriter("/Users/buzun/gb/data/data_all.arff")

    fw.write("@relation interests \n\n")

    Array.range(0, p).foreach(i => fw.write("@attribute F_"+i+" numeric \n"))

    table.interestsColumns.foreach(s => fw.write("@attribute class_"+s+" {0,1} \n"))

    fw.write("\n @data \n")

    UsersModel.getAllWithInterests.foreach(u => {

      usersIdsBuf += u.vkUser.vkId

      val Fs = Array.fill[Double](p)(0.0)
      u.vkUser.getText.split(" ").filter(s => (wc.getOrElse(s, 0) > 10) && w2cl.contains(s)).foreach(s => Fs(w2cl.get(s).get) += 1.0 / wc.get(s).get)

      val labels = u.interests.get.bin(1)

      fw.write(Fs.mkString(",") + "," + labels.mkString(",") +  "\n")
    })

    fw.close()
  }


  //mkArff()

  val usersIds = usersIdsBuf.toArray

  val source = new DataSource("/Users/buzun/gb/data/data_all.arff")
  val data = source.getDataSet

  val attrSel = new FeaturesExtractor(data)

  val fw = new FileWriter("/Users/buzun/gb/data/interest2attr.txt")

  table.interestsColumns.foreach(i => {
    val line = table.col2name(i) + "|" + i + "|" + attrSel.extract("class_" + i).dropRight(1).mkString(",")
    println(line)
    fw.write(line + "\n")
  })

  fw.close()

}
