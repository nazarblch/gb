import java.util.Random

import db.{InterestsModel, UsersModel}
import predictors.PLSAPredictor
import ru.ispras.modis.tm.attribute.DefaultAttributeType
import ru.ispras.modis.tm.builder.{FixedPhiBuilder, RobustPLSABuilder}
import ru.ispras.modis.tm.documents.{Numerator, Alphabet, TextualDocument}
import ru.ispras.modis.tm.initialapproximationgenerator.GibbsInitialApproximationGenerator
import ru.ispras.modis.tm.plsa.TrainedModelSerializer
import ru.ispras.modis.tm.regularizer.{TopicEliminatingRegularizer, DecorrelatingRegularizer, PredefPhiReg, PredefThetaReg}
import ru.ispras.modis.tm.sparsifier.CarefulSparcifier
import ru.ispras.modis.tm.utils.TopicHelper

import scala.collection.mutable.ArrayBuffer

object PLSAPredict extends App {


  val users = UsersModel.getAllWithInterests.toVector.filter(u => !UsersModel.bannedIds.contains(u.vkUser.vkId))

  val predictor = PLSAPredictor.apply()
  val res = predictor.rankInterests(users.map(_.vkUser))


  res.foreach(resi => {
    println(resi._1.vkId)
    println(resi._2.getTop(5).map({case(t, score) => (t.title, score)}))
    UsersModel.addRankedInterests(resi._1.vkId, resi._2)
  })



}