package db

import com.mongodb.casbah.Imports._
import predictors.RankedInterests


class DBUser(val vkUser: VKUser, val interests: Option[Interests]) {
  def toMongoObject: MongoDBObject = {
    MongoDBObject(
      DBUser.ID -> vkUser.vkId,
      DBUser.VKID -> vkUser.vkId,
      DBUser.TEXT -> vkUser.text,
      DBUser.GROUP_IDS -> vkUser.groups.map(_.id).toArray,
      DBUser.INTERESTS -> (if (interests.isDefined) interests.get.getData else None))
  }
}


object DBUser {

  val ID = "_id"
  val VKID = "vkid"
  val TEXT = "text"
  val NUM_TEXT = "num_text"
  val GROUP_IDS = "group_ids"
  val INTERESTS = "interests"

  def apply(obj: DBObject): DBUser = {

     val vku = new VKUser(obj.getAsOrElse[Int](VKID, throw new IllegalArgumentException("id has wrong type")),
      obj.getAsOrElse[String](TEXT, throw new IllegalArgumentException("text has wrong type")),
      obj.getAsOrElse[BasicDBList](GROUP_IDS, throw new IllegalArgumentException("groups has wrong type")).toArray()
      .map(i => GroupsModel.getById(i.toString.toInt)).toSeq)

    val interests = if (obj.get(INTERESTS) == null) None
      else Some (obj.getAsOrElse[BasicDBList](INTERESTS, throw new IllegalArgumentException("interests has wrong type")))

    new DBUser(vku, if(interests.isDefined) Some(Interests(interests.get)) else None)

  }

  def applyFast(obj: DBObject): DBUser = {
    val vku = new VKUser(obj.getAsOrElse[Int](VKID, throw new IllegalArgumentException("id has wrong type")),
      obj.getAsOrElse[String](TEXT, throw new IllegalArgumentException("text has wrong type")),
      null)

    val interests = if (obj.get(INTERESTS) == null) None
    else Some (obj.getAsOrElse[BasicDBList](INTERESTS, throw new IllegalArgumentException("interests has wrong type")))

    new DBUser(vku, if(interests.isDefined) Some(Interests(interests.get)) else None)

  }
}

class VKUser(val vkId: Int, val text: String, val groups: Seq[VKGroup]) {
  private def shortText = text.split(" ").take(4000).mkString(" ")
  def getText: String = (groups.map(_.getShortText(40)).mkString(" ") + " " + shortText).split(" ").filter(_.trim.length > 0).take(7000).mkString(" ")
  def getGroupsText: String = groups.map(_.getShortText(40)).mkString(" ")
}

class VKPost(val id: Int, date: String, val text: String, val ownerVkId: Int) {}

class VKGroup(val id: Int, val name: String, val text: String){
  def getShortText(wc: Int) = name + " " + text.split(" ").map(_.trim).take(wc).mkString(" ")

}


class TopicVKGroup(override val id: Int, override val name: String, override val text: String, val interets: Vector[DBInterest]) extends VKGroup(id, name, text) {
}

object VKGroup {

  val ID = "_id"
  val GID = "gid"
  val NAME = "name"
  val TEXT = "text"


  def apply(obj: DBObject): VKGroup = {
    new VKGroup(obj.getAsOrElse[Int](GID, throw new scala.IllegalArgumentException("id has wrong type")),
      obj.getAsOrElse[String](NAME, throw new scala.IllegalArgumentException("name has wrong type")),
      obj.getAsOrElse[String](TEXT, throw new scala.IllegalArgumentException("group text has wrong type"))
    )
  }
}

object TopicVKGroup {

  val INTERESTS = "interests"

  def apply(obj: DBObject): TopicVKGroup = {
    val g = VKGroup(obj)
    new TopicVKGroup(g.id, g.name, g.text,
      obj.getAsOrElse[BasicDBList](INTERESTS, throw new scala.IllegalArgumentException("interests has wrong type"))
        .toArray.toVector.map(_.toString.toInt).map(InterestsModel.id2interest))
  }
}


class Interests(private val data: Vector[(DBInterest, Int)], val category: Option[Int] = None) {

  assert(data.size == InterestsModel.getAll.size || category.isDefined)

  Range(1, data.size).foreach(i => assert(data(i)._1.id > data(i-1)._1.id))

  val id2score = data.map({case(k, v) => (k.id, v)}).toMap

  override def toString = data.map(i => (i._1.title, i._2)).mkString(",")

  def bin(c: Int = 1): Vector[Int] = data.map(x => if (x._2 > c) 1 else 0)

  def filterCategory(cat: Int): Interests = new Interests(data.filter(_._1.category == cat), Some(cat))

  def getData: Vector[(Int, Int)] = data.map({case (k, v) => (k.id, v)})

  def getBinData: Vector[(Int, Int)] = data.map(_._1.id).zip(bin(1))
}

object Interests {
  def apply(i: Int): Interests = {
    val a = InterestsModel.getAll.map(interest => if (interest.id == i) (interest, 2) else (interest, 0))
    new Interests(a)
  }

  def apply(v: Vector[(Int, Int)]): Interests = {
    v.indices.foreach(i => assert(v(i)._1 == InterestsModel.getAll(i).id))
    new Interests(InterestsModel.getAll.zip(v.map(_._2)))
  }

  def applySet(v: Vector[DBInterest]): Interests = {
    val set = v.map(_.id).toSet
    new Interests(InterestsModel.getAll.zip(InterestsModel.getAll.map(intrst => if(set.contains(intrst.id)) 2 else 0)))
  }

  def apply(v: BasicDBList): Interests = {
    apply(
      v.toArray.map(vi => vi.asInstanceOf[BasicDBList]).map(vi => (vi(0).toString.toInt, vi(1).toString.toInt)).toVector)
  }

}

class UsersModel(val dbname: String = DBConfig.dbname) {

  val table = "users"
  val db =  MongoConnection()(dbname)(table)

  def addUser(u: DBUser): Unit = {
    val q = MongoDBObject(DBUser.ID -> u.vkUser.vkId)
    if (db.find(q).count == 0) db += u.toMongoObject
  }

  def addNumericText(u: DBUser, text: Vector[Int]): Unit = {
    val q = MongoDBObject(DBUser.ID -> u.vkUser.vkId)
    val obj = u.toMongoObject
    obj.update(DBUser.NUM_TEXT, text.toArray)
    db.findAndModify(q, obj)
  }

  def updateInterests(vkId: Int, interests: Interests): Unit = {
    val q = MongoDBObject(DBUser.ID -> vkId)

    val obj_req = db.findOne(q)


    if(obj_req.isDefined) {
      val obj = obj_req.get
      obj.update(DBUser.INTERESTS, interests.getData)
      db.findAndModify(q, obj)
    } else {
      println(vkId + " not found")
    }
  }

  def addDataVector(vkId: Int, data: IndexedSeq[Double], field_name: String): Unit = {
    val q = MongoDBObject(DBUser.ID -> vkId)
    val obj_req = db.findOne(q)

    if(obj_req.isDefined) {
      val obj = obj_req.get
      obj.update(field_name, data)
      db.findAndModify(q, obj)
    } else {
      println(vkId + " not found")
    }
  }

  def addRankedInterests(vkId: Int, data: RankedInterests): Unit = {
    val q = MongoDBObject(DBUser.ID -> vkId)
    val obj_req = db.findOne(q)

    if(obj_req.isDefined) {
      val obj = obj_req.get
      obj.update("ranked_interests", data.getData)
      db.findAndModify(q, obj)
    } else {
      println(vkId + " not found")
    }
  }


  def drop(): Unit = {
    db.drop()
  }

  def getById(id: Int): DBUser = {
    val q = MongoDBObject(DBUser.ID -> id)
    DBUser(db.findOne(q).get)
  }

  def getAll: Iterator[DBUser] = {
    db.find().map(DBUser.apply)
  }

  def getAllFast: Iterator[DBUser] = {
    db.find().map(DBUser.applyFast)
  }

  def getAllFastWithInterests: Iterator[DBUser] = {
    db.find().filter(_.get(DBUser.INTERESTS) != null).map(DBUser.applyFast)
  }

  def getAllWithInterests: Iterator[DBUser] = {
    db.find().filter(_.get(DBUser.INTERESTS) != null).map(DBUser.apply)
  }

}


object UsersModel extends UsersModel(DBConfig.dbname) {

  val bannedIds: Set[Int] = io.Source.fromFile("/Users/buzun/gb/data/banned_users.txt").getLines().map(_.trim.toInt).toSet

}



class GroupsModel(val dbname: String = DBConfig.dbname) {

  val table = "groups"
  val db =  MongoConnection()(dbname)(table)

  def drop(): Unit = {
    db.drop()
  }

  def addGroup(g: VKGroup): Unit = {
    val obj = MongoDBObject(
      VKGroup.ID -> g.id,
      VKGroup.GID -> g.id,
      VKGroup.NAME -> g.name,
      VKGroup.TEXT -> g.text)

    val q = MongoDBObject(VKGroup.ID -> g.id)

    if (db.find(q).count == 0) db += obj
  }

  def getById(id: Int): VKGroup = {
    val q = MongoDBObject(VKGroup.ID -> id)
    VKGroup(db.findOne(q).get)
  }

  def getAll: Iterator[VKGroup] = {
    db.find().map(VKGroup.apply)
  }

}

object GroupsModel extends GroupsModel(DBConfig.dbname) {

}

object TopicGroupsModel  {
   val table = "topic_groups"
   val db =  MongoConnection()(DBConfig.dbname)(table)

  def drop(): Unit = {
    db.drop()
  }

  def getAll: Iterator[TopicVKGroup] = {
    db.find().map(TopicVKGroup.apply)
  }

   def addGroup(g: VKGroup, interests: Vector[DBInterest]): Unit = {
    val obj = MongoDBObject(
      VKGroup.ID -> g.id,
      VKGroup.GID -> g.id,
      VKGroup.NAME -> g.name,
      VKGroup.TEXT -> g.text,
      TopicVKGroup.INTERESTS -> interests.map(_.id))

    val q = MongoDBObject(VKGroup.ID -> g.id)

    if (db.find(q).count == 0) db += obj
  }
}

class DBInterest(val id: Int, val title: String, val keyWords: Set[String], val category: Int) {
  def toMongoObject = MongoDBObject(
    DBInterest.ID -> id,
    DBInterest.TITLE -> title,
    DBInterest.KEY_WORDS -> keyWords.toArray,
    DBInterest.CATEGORY -> category)
}

object DBInterest {
  val ID = "_id"
  val TITLE = "title"
  val KEY_WORDS = "key_words"
  val CATEGORY = "category"

  def apply(obj: DBObject): DBInterest = {
    new DBInterest(obj.getAsOrElse[Int](ID, throw new scala.IllegalArgumentException("id has wrong type")),
      obj.getAsOrElse[String](TITLE, throw new scala.IllegalArgumentException("title has wrong type")),
      obj.getAsOrElse[BasicDBList](KEY_WORDS, throw new scala.IllegalArgumentException("key words has wrong type")).toArray.map(_.asInstanceOf[String]).toSet,
      obj.getAsOrElse[Int](CATEGORY, throw new scala.IllegalArgumentException("id has wrong type"))
    )
  }
}

object InterestsModel  {
  val table = "interests"
  val db =  MongoConnection()(DBConfig.dbname)(table)

  def drop(): Unit = {
    db.drop()
  }

  val getAll: Vector[DBInterest] = {
    db.find().map(DBInterest.apply).toVector.sortBy(_.id)
  }

  val id2interest: Map[Int, DBInterest] = getAll.map(i => (i.id, i)).toMap
  val title2interest: Map[String, DBInterest] = getAll.map(i => (i.title, i)).toMap

  def getAllWithCat(cat: Int): Vector[DBInterest] = {
    getAll.filter(_.category == cat)
  }

  def getInterestByStr(s: String): Option[DBInterest] = {
    var res = getAll.filter(int => s.split("\\/")(0).split(" ").forall(si => int.title contains si.trim))
    if (res.length > 1) res = res.filter(int => int.title equals s.trim)
    if (res.length == 1) Some(res.head)
    else None
  }

  def addInterest(interest: DBInterest): Unit = {
    val obj = interest.toMongoObject

    val q = MongoDBObject(DBInterest.ID -> interest.id)

    if (db.find(q).count == 0) db += obj
  }

  def addKW(s: String, kw: Set[String]): Unit = {
    val interest = getInterestByStr(s)

    if (interest.isDefined) {
      val q = MongoDBObject(DBInterest.ID -> interest.get.id)
      val obj = interest.get.toMongoObject
      obj.update(DBInterest.KEY_WORDS, kw)
      db.findAndModify(q, obj)
    }

  }


}


