import db.{DBInterest, InterestsModel, TopicGroupsModel}
import load.vk.API
import load.{TopicsVKGroupsTable, GoogleTable, WishesReader, UserById}
import textproc.{Distance, Text2VecSeq}
import util.JSONParcer

/**
 * Created by nazar on 7/2/15.
 */
object TestData extends App {


  TopicGroupsModel.drop()



  //val gr = API.getGroup("club12208099")

  val table = new TopicsVKGroupsTable()

  val title2gr = table.activeCols.map(c => table.getWordsWithHeader(c))

  val gr2titles = title2gr.flatMap({case(title, gnames) => gnames.map(name => (name, title))}).groupBy(_._1).mapValues(_.map(_._2))

  gr2titles.foreach({case(gname, titles) =>
      val interests: Vector[DBInterest] = titles.map(InterestsModel.getInterestByStr).map(_.get)
      val gr = API.getGroup(gname)
      println(gname, gr)
      if (gr.isDefined) TopicGroupsModel.addGroup(gr.get, interests)
  })

}
