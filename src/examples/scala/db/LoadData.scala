package db

import load.{GoogleTable, VkData}
import util.{MapMerger, JSONParcer}


object LoadData extends App {

  val table = new GoogleTable()

   val groups = VkData.json_user2groups
   val u2gr: Map[Int, Seq[VKGroup]] = groups.mapValues(
     arr => JSONParcer.findAll(arr, Seq("gid", "name", "description")).map(seq => new VKGroup(seq(0).toInt, seq(1), seq(2)))
   )

   val walls = VkData.json_user2wall
   val users: Map[Int, String] = walls.mapValues(
     arr => JSONParcer.findAll(arr, "text").mkString(" . ")
   )

  val vkUsers: Map[Int, VKUser] = MapMerger(users, u2gr).map({ case(id, (text, grs)) =>
    id -> new VKUser(id, text, grs.getOrElse(Seq()))
  })

  UsersModel.drop()

  MapMerger(vkUsers, table.vkId2Interests).foreach({ case(id, (u, interests)) =>
    val dbUser = new DBUser(u, interests.get)
    UsersModel.addUser(dbUser)
  })




//
//  val vkgr = groups.values.toVector.flatMap(JSONParcer.findAll(_, Seq("gid", "name", "description"))).map(seq => new VKGroup(seq(0).toInt, seq(1), seq(2)))
//
//  vkgr.foreach(g => GroupsModel.addGroup(g))



}
