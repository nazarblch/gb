package load


/**
 * Created by nazar on 7/2/15.
 */
object UserById {

  def get(id: Int): String = {
    val vkid = IdVkReader.id2vk.get(id).get
    io.Source.fromFile("/home/nazar/gb/data/Users.txt").getLines().find(_.split(" ")(0).toInt == vkid).get
  }

}
