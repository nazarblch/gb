
import java.io.{File, FileWriter}
import java.util.Random

import db.{Interests, InterestsModel, UsersModel, TopicGroupsModel}
import load.GoogleTable
import ru.ispras.modis.tm.attribute.DefaultAttributeType
import ru.ispras.modis.tm.builder.RobustPLSABuilder
import ru.ispras.modis.tm.documents.{Alphabet, Numerator, TextualDocument}
import ru.ispras.modis.tm.initialapproximationgenerator.GibbsInitialApproximationGenerator
import ru.ispras.modis.tm.matrix.Theta
import ru.ispras.modis.tm.plsa.TrainedModelSerializer
import ru.ispras.modis.tm.regularizer.{PredefPhiReg, TopicEliminatingRegularizer, DecorrelatingRegularizer, PredefThetaReg}
import ru.ispras.modis.tm.sparsifier.CarefulSparcifier
import ru.ispras.modis.tm.utils.TopicHelper
import textproc.Dictionary
import textproc.plsa._

import scala.io.Source

object PLSA extends App {


  val users = UsersModel.getAllWithInterests.toVector.filter(u => !UsersModel.bannedIds.contains(u.vkUser.vkId))
  // val tgroups = TopicGroupsModel.getAll.toVector.filter(_.id < -10)


  def getTextualDocuments: Iterator[TextualDocument] = {

    val lines = users.map(_.vkUser.getText) // ++ tgroups.map(_.getShortText(2000))

    val wordsSequence = lines.map(line => line.split(" "))
    val textualDocuments = wordsSequence.map(words => new TextualDocument(Map(DefaultAttributeType -> words)))

    textualDocuments.toIterator
  }

  def getTheta0Documents(topic_count: Int, offset: Int = 0): Array[Array[Float]] = {

    var j = 0
    val res = users.map(u => {
      val row = Array.fill(topic_count)(0f)
      if (j >= offset) u.interests.get.getBinData.foreach({case (ind, score) =>
        row(ind) += score
      })
      j += 1
      row
    }).toArray

    res
  }

  def getPhi0(topic_count: Int, alphabet: Alphabet): Array[Array[Float]] = {

    val res = Array.fill(topic_count)(Array.fill(alphabet.numberOfWords(DefaultAttributeType))(0f))

    InterestsModel.getAll.foreach(interest => {
      interest.keyWords.flatMap(_.split(" ")).map(_.trim).map(alphabet.getIndex).filter(_.isDefined)
        .foreach(pos => res(interest.id)(pos.get) = 1f)
    })

    res
  }


  /**
   * read textual documents from file (see functions getTextualDocuments for details)
   */
  val textualDocuments = getTextualDocuments

  val (documents, alphabet) = Numerator(textualDocuments)

  val numberOfTopics = 230
  val theta0Reg = new PredefThetaReg(2f, getTheta0Documents(numberOfTopics))
  val phi0Reg = new PredefPhiReg(15f, getPhi0(numberOfTopics, alphabet))
  /**
   * val splitLines = Source.fromFile(new File("examples/arxiv.part")).getLines().map(_.split(" "))
   * val (documents, alphabet) = Numerator(splitLines)
   */


  /**
   * now we have to build model. In this example we would use a plsa
   * we use builder to build instance of class PLSA
   * it require define number of topics, number of iterations, alphabet, sequence of documents and random number generator
   * to generate initial approximation
   */

  val numberOfIteration = 50
  // number of iteration in EM algorithm
  val random = new Random()
  // java.util.Random
  val builder = new RobustPLSABuilder(numberOfTopics, alphabet, documents, numberOfIteration = numberOfIteration, backgroundWeight = 0.3f)
    .addRegularizer(new DecorrelatingRegularizer(40))
    .addRegularizer(new TopicEliminatingRegularizer(documents, 10))
    .addRegularizer(theta0Reg)
    .addRegularizer(phi0Reg)
    .setThetaSparsifier(new CarefulSparcifier(0.03f, 15, 5))
    .setPhiSparsifier(new CarefulSparcifier(0.0003f, 15, 20))
    .setInitialApproximationGenerator(new GibbsInitialApproximationGenerator(random))


  /**
   * and now we build plsa
   */
  val plsa = builder.build()

  /**
   * now we have documents and model and we may train model. Our model take into input sequence of documents and
   * perform stochastic matrix decomposition F ~ Phi * Theta where Phi is distribution of words by topics, thus
   * the number in the intersects of i-th row and j-th column show the probability to generate word j from topic i.
   * Theta is distribution of document by topic thus the number in the intersects of i-th row and j-th column show
   * the weight of topic j in document i.
   * The result would be saved in TrainedModel
   */
  val trainedModel = plsa.train

  /**
   * now we obtain matrix of distribution of words by topics and we may see most popular words from each topic
   * For this purpose we use util printAllTopics. It print n words with the highest probability from every topic.
   */
  val n = 15 // number of top words to see
  TopicHelper.printAllTopics(n, trainedModel.phi(DefaultAttributeType), alphabet)

  val tops = trainedModel.theta.tops(3)

  val qual = users.indices.map(i => {
    val res = tops(i).map(ti => users(i).interests.get.id2score.get(ti))
    (tops(i).mkString(","), res.mkString(","), res.count(_.getOrElse(0) == 2))
  }).map(_._3).take(100).sum


  println(qual)

  val lusers = users.indices.map(i => {
    val res = tops(i).map(ti => users(i).interests.get.id2score.getOrElse(ti, 0))
    (users(i).vkUser.vkId, res.sum)
  }).sortBy(_._2).foreach(println)

  /**
   * now we save matrix Phi (words by topic ) into textual file examples/Phi
   * and matrix Theta (topic by document) in file textual examples/Theta
   */
  TopicHelper.saveMatrix("/Users/buzun/tm/examples/Phi", trainedModel.phi(DefaultAttributeType))
  TopicHelper.saveMatrix("/Users/buzun/tm/examples/Theta", trainedModel.theta)

  /**
   * and now we can serialize trainedModel using kryo. Kryo saves objects in binary format, so do not try to open
   * model by textual redactor.
   */
  TrainedModelSerializer.save(trainedModel, "/Users/buzun/tm/examples/model")

}
