package load.vk

import java.io.{BufferedInputStream, DataInputStream}
import java.net.URL

import edu.depauw.csc.dcheeseman.wgetjava.WGETJava
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.VkontakteApi
import org.scribe.model.{OAuthRequest, Verb, Verifier, Token}

import scala.io.Source
import scala.util.parsing.json



object API {

  val NETWORK_NAME = "Vkontakte.ru"

  val TOKEN: Token  = new Token("b452510bd9653a239fc484d7795a9c71b93dd41c3c0ff2458579e276187b48bd8c51e2bf5e27073331a20", "")

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
