package load.vk

import java.io.{BufferedInputStream, DataInputStream}
import java.net.URL

import db.VKGroup
import edu.depauw.csc.dcheeseman.wgetjava.WGETJava
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.VkontakteApi
import org.scribe.model.{OAuthRequest, Verb, Verifier, Token}
import spray.json.JsValue

import spray.json._
import DefaultJsonProtocol._
import util.JSONParcer


object API {

  val NETWORK_NAME = "Vkontakte.ru"

  val TOKEN: Token  = new Token("b452510bd9653a239fc484d7795a9c71b93dd41c3c0ff2458579e276187b48bd8c51e2bf5e27073331a20", "")

  def execMethod(name: String, fields: Map[String, Any]): JsValue = {

    val PROTECTED_RESOURCE_URL = "https://api.vk.com/method/"+name+"?" + fields.toSeq.map({case (k, v) => k + "=" + v.toString}).mkString("&") + "&v=5.8"
    // Replace these with your own api key and secret
    val clientId = "2184846"
    val apiSecret = "5r5rAafxLAEUYywwdv9A"
    val service = new ServiceBuilder()
      .provider(classOf[VkontakteApi])
      .apiKey(clientId)
      .apiSecret(apiSecret)
      .callback("http://localhost")
      .build()

    val request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL)
    service.signRequest(TOKEN, request)
    val response = request.send()

    //println(response.getBody.mkString)

    assert(response.getCode == 200)

    response.getBody.mkString.parseJson

  }

  def searchGroups(q: String, n: Int): Seq[(Int, String, Boolean)] =  {
    JSONParcer.findAll(execMethod("groups.search", Map("q" -> q.split(" ").mkString("/"), "count" -> n)), Seq("id", "name", "is_closed")).result()
    .toSeq.map(seq => (seq(0).toInt, seq(1), if (seq(2).toInt == 0) false else true))

  }

  def getGroup(q: String): Option[VKGroup] =  {
    val res = JSONParcer.findAll(execMethod("groups.getById", Map("group_id" -> q, "fields" -> "description")), Seq("id", "name", "description", "is_closed")).result()
      .toSeq.map(seq => (seq(0).toInt, seq(1), seq(2), if (seq(3).toInt == 0) false else true))

    if (res.nonEmpty) {
      val gid = res.head._1
      val text = res.head._3 + "| " + getGroupPosts(gid, 300).mkString("| ")

      Some(new VKGroup(gid, res.head._2, text))
    } else {
      None
    }
  }

  def getGroupPosts(id: Int, n: Int): Seq[String] =  {
    JSONParcer.findAll(
    execMethod("wall.get", Map("owner_id" -> -id, "count" -> n)),
    "text").result().toSeq
  }

  def getGroups(q: String, n: Int): Seq[VKGroup] =  {
    searchGroups(q, n).filter(!_._3).map({case(id, name, flag) =>
      val text = getGroupPosts(id, 500)
      new VKGroup(id, name, text.mkString("| "))
    })
  }

  def searchUserByName(name: String): Option[Int] = {

    val PROTECTED_RESOURCE_URL = "https://api.vk.com/method/users.get?user_ids=" + name.split("\\/").last + "&v=5.8"
    // Replace these with your own api key and secret
    val clientId = "2184846"
    val apiSecret = "5r5rAafxLAEUYywwdv9A"
    val service = new ServiceBuilder()
      .provider(classOf[VkontakteApi])
      .apiKey(clientId)
      .apiSecret(apiSecret)
      .callback("http://localhost")
      .build()

    val request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL)
    service.signRequest(TOKEN, request)
    val response = request.send()


    try {

      assert(response.getCode == 200)


    } catch  {
      case e: java.lang.AssertionError => return None
    }

    println(response.getBody.mkString)

    val res = response.getBody.mkString

    try {
    val pos: Int =
      "\"id\":".r.findFirstMatchIn(res).get.start

    Some(res.substring(pos + 5, pos + 15).split(",")(0).toInt)

    } catch  {
      case e: Exception => None
    }

  }

  def getVkId(str: String): Option[Int] = {
    if (str.trim.startsWith("http") || str.trim.startsWith("vk.com")) getVKPage(str)
    else if (str.trim.forall(c => c.isDigit || c.equals('.') || c.equals('E')))  getVKPage("http://vk.com/id" + str.trim.toDouble.toInt.toString)
    else  getVKPage("http://vk.com/" + str.trim)
  }

  def getVKPage(url: String): Option[Int] = {
    try {

      val u = new URL(url)
      val is = u.openStream()
      val dis = new DataInputStream(new BufferedInputStream(is))

      var s = ""
      var res = ""

      while (s != null) {
        s = dis.readLine()
        res += s + "\n"
      }



      println(res)

      val pos =
        "/wall".r.findFirstMatchIn(res.toString)

      if (pos.isEmpty) return None

      val posb = pos.get.start
      Some(res.toString.substring(posb + 5, posb + 20).split("\\_")(0).toInt)

    } catch {
      case e: java.io.FileNotFoundException => None
      case e: java.net.MalformedURLException => None
      case e: java.nio.charset.MalformedInputException => None
      case e: java.lang.NumberFormatException => None
    }
  }

}
