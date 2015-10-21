package db

import load.{GoogleTable, VkData}
import util.{MapMerger, JSONParcer}


object LoadData extends App {

    val table = new GoogleTable()
//
//  UsersModel.drop()
//
//  MapMerger(VkData.id2VkUser, table.vkId2Interests).foreach({ case(id, (u, interests)) =>
//    val dbUser = new DBUser(u, interests)
//    UsersModel.addUser(dbUser)
//  })


//
//  GroupsModel.drop()
//  val vkgr = groups.values.toVector.flatMap(JSONParcer.findAll(_, Seq("gid", "name", "description"))).map(seq => new VKGroup(seq(0).toInt, seq(1), seq(2)))
//
//  vkgr.foreach(g => GroupsModel.addGroup(g))


  // VkData.groups.values.foreach(g => GroupsModel.addGroup(g))

//  VkData.users(100000).foreach(u => {
//    val dbu = new DBUser(u, None)
//    new UsersModel().addUser(dbu)
//  })


  table.vkId2Interests.foreach({case (vkId, interests) =>
    UsersModel.updateInterests(vkId, interests)
  })

}
