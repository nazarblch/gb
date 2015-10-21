package util

import spray.json._
import DefaultJsonProtocol._

import scala.collection.mutable.ArrayBuffer

/**
 * Created by buzun on 06/08/15.
 */
object JSONParcer {

  def findAll(jsVal: JsValue, name: String): ArrayBuffer[String] = {
    val buf: ArrayBuffer[String] = ArrayBuffer()

    if(jsVal.isInstanceOf[JsArray]) {
      jsVal.convertTo[JsArray].elements.foreach(v => buf ++= findAll(v, name))
    } else if (jsVal.isInstanceOf[JsObject]) {

      jsVal.asJsObject.fields.foreach({ case (k, v) =>
        if ((k equals name) && v.isInstanceOf[JsString]) buf += v.convertTo[String]
        else if (v.isInstanceOf[JsObject] || v.isInstanceOf[JsArray]) buf ++= findAll(v, name)
      })

    }

    buf
  }

  def findAll(jsVal: JsValue, names: Seq[String]): ArrayBuffer[Seq[String]] = {
    val buf: ArrayBuffer[Seq[String]] = ArrayBuffer()

    if(jsVal.isInstanceOf[JsArray]) {
      jsVal.convertTo[JsArray].elements.foreach(v => buf ++= findAll(v, names))
    } else {

      val flds = jsVal.asJsObject.fields

      if (names.forall(flds.contains)) {

        buf += names.map(s => flds.get(s).get.toString)

      } else {

        flds.foreach({ case (k, v) =>
          if (v.isInstanceOf[JsObject] || v.isInstanceOf[JsArray]) buf ++= findAll(v, names)
        })

      }

    }

    buf
  }

}
