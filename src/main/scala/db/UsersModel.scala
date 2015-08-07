package db

import com.mongodb.casbah.Imports._


class DBUser(val vkUser: VKUser, val interests: Interests) {
  def toMongoObject: MongoDBObject = {
    MongoDBObject(
      UsersModel.VKID -> vkUser.vkId,
      UsersModel.TEXT -> vkUser.text,
      UsersModel.GROUP_IDS -> vkUser.groups.map(_.id).toArray,
      UsersModel.INTERESTS -> interests.data.toArray)
  }
}

class VKUser(val vkId: Int, val text: String, val groups: Seq[VKGroup]) {}

class VKPost(val id: Int, date: String, val text: String, val ownerVkId: Int) {}

class VKGroup(val id: Int, val name: String, val text: String){}

class Interests(val data: Vector[Int]) {
  assert(data.size == DBConfig.interestsSize)
  override def toString = data.mkString(",")
}

object UsersModel {

  val ID = "_id"
  val VKID = "vkid"
  val TEXT = "text"
  val GROUP_IDS = "group_ids"
  val INTERESTS = "interests"

  val table = "users"
  val db =  MongoConnection()(DBConfig.dbname).getCollection(table)

  def addUser(u: DBUser): Unit = {
    val q = MongoDBObject(VKID -> u.vkUser.vkId)
    if (db.find(q).count() == 0) db.insert(u.toMongoObject)
  }

  def drop(): Unit = {
    db.drop()
  }

}

object GroupsModel {

  val ID = "_id"
  val GID = "gid"
  val NAME = "name"
  val TEXT = "text"

  val table = "groups"
  val db =  MongoConnection()(DBConfig.dbname).getCollection(table)

  def drop(): Unit = {
    db.drop()
  }

  def addGroup(g: VKGroup): Unit = {
    val obj = MongoDBObject(
      GID -> g.id,
      NAME -> g.name,
      TEXT -> g.text)

    val q = MongoDBObject(GID -> g.id)

    if (db.find(q).count() == 0) db.insert(obj)
  }



}


