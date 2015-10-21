package predictors

import java.util.Random

import db.{InterestsModel, DBUser}
import ru.ispras.modis.tm.attribute.DefaultAttributeType
import ru.ispras.modis.tm.builder.RobustPLSABuilder
import ru.ispras.modis.tm.documents.{Numerator, Alphabet, TextualDocument}
import ru.ispras.modis.tm.initialapproximationgenerator.GibbsInitialApproximationGenerator
import ru.ispras.modis.tm.plsa.TrainedModelSerializer
import ru.ispras.modis.tm.regularizer.{TopicEliminatingRegularizer, DecorrelatingRegularizer, PredefPhiReg, PredefThetaReg}
import ru.ispras.modis.tm.sparsifier.CarefulSparcifier
import ru.ispras.modis.tm.utils.TopicHelper


class PLSABuilder extends Builder {

  def getPhi0(topic_count: Int, alphabet: Alphabet): Array[Array[Float]] = {

    val res = Array.fill(topic_count)(Array.fill(alphabet.numberOfWords(DefaultAttributeType))(0f))

    InterestsModel.getAll.foreach(interest => {
      interest.keyWords.flatMap(_.split(" ")).map(_.trim).map(alphabet.getIndex).filter(_.isDefined)
        .foreach(pos => res(interest.id)(pos.get) = 1f)
    })

    res
  }

  def getTheta0(topic_count: Int, usersWithInterests: Vector[DBUser]): Array[Array[Float]] = {

    val res = usersWithInterests.map(u => {
      val row = Array.fill(topic_count)(0f)
      u.interests.get.getBinData.foreach({case (ind, score) =>
        assert(score == 1 || score == 0)
        row(ind) = score
      })
      row
    }).toArray

    res
  }


  def train(usersWithInterests: Vector[DBUser], numberOfTopics: Int): PLSAPredictor = {

    val textualDocuments = PLSAPredictor.getTextualDocumentsWallGroups(usersWithInterests.map(_.vkUser))
    val (documents, alphabet) = Numerator(textualDocuments)

    val numberOfTopics = 230
    val theta0Reg = new PredefThetaReg(2f, getTheta0(numberOfTopics, usersWithInterests))
    val phi0Reg = new PredefPhiReg(15f, getPhi0(numberOfTopics, alphabet))

    val numberOfIteration = 50
    // number of iteration in EM algorithm
    val random = new Random()

    val builder = new RobustPLSABuilder(numberOfTopics, alphabet, documents, numberOfIteration = numberOfIteration, backgroundWeight = 0.3f)
      .addRegularizer(new DecorrelatingRegularizer(40))
      .addRegularizer(new TopicEliminatingRegularizer(documents, 10))
      .addRegularizer(theta0Reg)
      .addRegularizer(phi0Reg)
      .setThetaSparsifier(new CarefulSparcifier(0.03f, 15, 5))
      .setPhiSparsifier(new CarefulSparcifier(0.0003f, 15, 20))
      .setInitialApproximationGenerator(new GibbsInitialApproximationGenerator(random))


    val plsa = builder.build()

    val trainedModel = plsa.train

    TopicHelper.saveMatrix(PLSAPredictor.PHI_PATH, trainedModel.phi(DefaultAttributeType))
    TopicHelper.saveMatrix(PLSAPredictor.THETA_PATH, trainedModel.theta)

    TrainedModelSerializer.save(trainedModel, PLSAPredictor.MODEL_PATH)

    new PLSAPredictor(trainedModel)
  }

  override def train(usersWithInterests: Vector[DBUser]): Predictor = train(usersWithInterests, 230)
}
