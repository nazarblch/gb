package predictors

import db.{InterestsModel, VKGroup, VKUser}
import ru.ispras.modis.tm.attribute.DefaultAttributeType
import ru.ispras.modis.tm.builder.FixedPhiBuilder
import ru.ispras.modis.tm.documents.{Numerator, Alphabet, TextualDocument}
import ru.ispras.modis.tm.plsa.{TrainedModelSerializer, TrainedModel}
import ru.ispras.modis.tm.regularizer.TopicEliminatingRegularizer
import ru.ispras.modis.tm.sparsifier.CarefulSparcifier

class PLSAPredictor(model: TrainedModel) extends Predictor {

  override def rankInterests(users: IndexedSeq[VKUser]): IndexedSeq[(VKUser, RankedInterests)] = {

    val alph =  Alphabet(model.alphabet)
    val documents = Numerator(PLSAPredictor.getTextualDocumentsWallGroups(users), alph).toArray

    val fixedPLSA = new FixedPhiBuilder(alph, documents, 20, model.phi, model.attributeWeight, model.background)
      .addRegularizer(new TopicEliminatingRegularizer(documents, 5))
      .setThetaSparsifier(new CarefulSparcifier(0.01f, 10, 5))
      .build()


    val (predicted, theta) = fixedPLSA.predictTheta()

    users.indices.map(i => {
      val user = users(i)
      val topics = theta.getRow(i)
      val norm = InterestsModel.getAll.map(interest => topics(interest.id)).sum.toDouble
      // assert(norm > 0.1)
      (user, new RankedInterests(InterestsModel.getAll.map(i => (i, topics(i.id)/norm))))
    })

  }
}


object PLSAPredictor {

  // todo: find group preferences than find interests for each group

  val PHI_PATH = "/Users/buzun/tm/examples/Phi"
  val THETA_PATH = "/Users/buzun/tm/examples/Theta"
  val MODEL_PATH = "/Users/buzun/tm/examples/model"

  def apply(): PLSAPredictor = {
    val model = TrainedModelSerializer.load(MODEL_PATH)
    new PLSAPredictor(model)
  }

  def getTextualDocumentsWallGroups(users: IndexedSeq[VKUser]): Iterator[TextualDocument] = {
    val lines = users.map(_.getText)
    toTextualDocuments(lines)
  }

  def getTextualDocumentsWall(users: IndexedSeq[VKUser]): Iterator[TextualDocument] = {
    val lines = users.map(_.text)
    toTextualDocuments(lines)
  }

  def getTextualDocuments(groups: IndexedSeq[VKGroup]): Iterator[TextualDocument] = {
    val lines = groups.map(_.getShortText(500))
    toTextualDocuments(lines)
  }

  def toTextualDocuments(lines: IndexedSeq[String]): Iterator[TextualDocument] = {

    val wordsSequence = lines.map(line => line.split(" "))
    val textualDocuments = wordsSequence.map(words => new TextualDocument(Map(DefaultAttributeType -> words)))

    textualDocuments.toIterator
  }
}
