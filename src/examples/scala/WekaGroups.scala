import java.io.FileWriter
import java.util.Random
import java.util.function.Consumer

import db.{UsersModel, DBConfig}
import load.GoogleTable
import weka.classifiers.{Classifier, Evaluation}
import weka.classifiers.functions.{LibLINEAR, LibSVM}
import weka.classifiers.trees.RandomForest
import weka.core.SelectedTag
import weka.core.converters.ConverterUtils.DataSource
import de.bwaldvogel.liblinear._

import scala.collection.mutable.ArrayBuffer

/**
 * Created by buzun on 10/08/15.
 */
object WekaGroups extends App {

  val w2cl: Map[String, Int] = io.Source.fromFile("/Users/buzun/trunk/w2cl.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap

  val wc: Map[String, Int] = io.Source.fromFile("/Users/buzun/trunk/wc.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap

  val cols = Array(183,189,405,422,579,669,670,791,807,825,943,974,1100,1104,1157,1180,1249,1262,1281,1403,1469,1481,1552,1562,1582,1591,1626,1659,1726,1795,1870,1949,1986)

  val usersIdsBuf: ArrayBuffer[Int] = ArrayBuffer()

  def mkArff(): Unit = {



  }

mkArff()



}
