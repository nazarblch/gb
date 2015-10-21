import java.io.FileWriter
import java.util.Random
import java.util.function.Consumer

import db.{UsersModel, DBConfig}
import load.GoogleTable
import weka.classifiers.{Classifier, Evaluation}
import weka.classifiers.functions.{SMO, LibLINEAR, LibSVM}
import weka.classifiers.trees.RandomForest
import weka.core.SelectedTag
import weka.core.converters.ConverterUtils.DataSource
import de.bwaldvogel.liblinear._

import scala.collection.mutable.ArrayBuffer

/**
 * Created by buzun on 10/08/15.
 */
object Weka extends App {

  val w2cl: Map[String, Int] = io.Source.fromFile("/Users/buzun/trunk/w2cl.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap

  val wc: Map[String, Int] = io.Source.fromFile("/Users/buzun/trunk/wc.txt").getLines().toVector
    .map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).toMap

  val cols = Array(183,189,405,422,579,669,670,791,807,825,943,974,1100,1104,1157,1180,1249,1262,1281,1403,1469,1481,1552,1562,1582,1591,1626,1659,1726,1795,1870,1949,1986)

  val usersIdsBuf: ArrayBuffer[Int] = ArrayBuffer()

  def mkArff(): Unit = {

    val fw = new FileWriter("/Users/buzun/gb/data/data.arff")

    fw.write("@relation interests \n\n")

    Array.range(0, cols.length).foreach(i => fw.write("@attribute F_"+i+" numeric \n"))

    fw.write("@attribute class {0,1} \n")

    fw.write("\n @data \n")

    var c: Int = 0


    UsersModel.getAllWithInterests.foreach(u => {

      usersIdsBuf += u.vkUser.vkId

      val Fs = Array.fill[Double](2000)(0.0)

        val words = u.vkUser.getText.split(" ").filter(s => (wc.getOrElse(s, 0) > 10) && w2cl.contains(s))
        words.foreach(s => Fs(w2cl.get(s).get) += (1.0 / wc.get(s).get) / words.size)

      val label = u.interests.get.bin(1)(35)

      c += label

      fw.write(cols.map(i => Fs(i - 1)).mkString(",") + "," + label +  "\n")
    })


    println(c)

    fw.close()
  }


   mkArff()

  val usersIds = usersIdsBuf.toArray

  val source = new DataSource("/Users/buzun/gb/data/data.arff");
  val data = source.getDataSet()

  if (data.classIndex() == -1) {
    data.setClassIndex(cols.length)
  }

  def init() = {
    val forest = new SMO()
    //forest.setOptions(Array("-S", "2"))
    //forest.setCost(1)


    //forest.setNormalize(true)

//    val forest = new RandomForest
//    forest.setMaxDepth(5)

    /*
    *     0 -- L2-regularized logistic regression (primal)
    *     1 -- L2-regularized L2-loss support vector classification (dual)
    *     2 -- L2-regularized L2-loss support vector classification (primal)
    *     3 -- L2-regularized L1-loss support vector classification (dual)
    *     4 -- support vector classification by Crammer and Singer
    *     5 -- L1-regularized L2-loss support vector classification
    */

    forest.buildClassifier(data)

    forest
  }

  def crossValidate(): Double = {
    val forest = init()

    val eval = new Evaluation(data);
    eval.crossValidateModel(forest, data, 5, new Random(1));

    println(eval.toSummaryString)

    eval.correct()
  }

  def test() {
    val test_data = source.getDataSet();
    if (test_data.classIndex() == -1) {
      test_data.setClassIndex(cols.length);
    }


    val eval = new Evaluation(data);

    eval.evaluateModel(init(), test_data);

    val cl = init()

    Range(0, test_data.numInstances()).foreach(i => {
      val ref = test_data.get(i).classValue()
      val pred: Int = cl.classifyInstance(test_data.get(i)).round.toInt
      if (ref.toInt == 1 && pred.toInt == 0) println(usersIds(i) + " ref = " + ref + " pred = " + pred)

    })

    println(eval.toSummaryString)
  }

  println(new GoogleTable().getInterestName(35))

  crossValidate()
  test()



}
