import java.io.FileWriter

import db._
import textproc.{TrashFilter, Tokenizer}
import util.WC

/**
 * Created by buzun on 08/08/15.
 */
object CleanText extends App {

  val tok = new Tokenizer

  def cleanUser(u: VKUser): VKUser = {
    val res = TrashFilter(tok(u.text))
    new VKUser(u.vkId, res.mkString(" "), u.groups)
  }

  def cleanUser(u: DBUser): DBUser = {
    new DBUser(cleanUser(u.vkUser), u.interests)
  }

  def cleanGroup(g: VKGroup): VKGroup = {
    val res = TrashFilter(tok(g.text))
    new VKGroup(g.id, tok(g.name).mkString(" "), res.mkString(" "))
  }

  def cleanTopicGroup(g: TopicVKGroup): TopicVKGroup = {
    val res = TrashFilter(tok(g.text))
    new TopicVKGroup(g.id, tok(g.name).mkString(" "), res.mkString(" "), g.interets)
  }



  val cl_gr = TopicGroupsModel.getAll.toVector.map(g => {
    cleanTopicGroup(g)
  })

  TopicGroupsModel.drop()
  cl_gr.foreach(g => TopicGroupsModel.addGroup(g, g.interets))


}
