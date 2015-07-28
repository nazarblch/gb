import java.io.FileWriter

import load.VkData
import user.{MetricUser, User, Group}

object CollectUsers extends App {

  val users = VkData.users

  println(users.size)

  val fw = new FileWriter("/home/nazar/gb/data/Users.txt")

  users.foreach(u => fw.write(u.id + " " + u.getText + "\n"))

  fw.close()

  val rn_users = users.map(MetricUser.apply)

  val fw1 = new FileWriter("/home/nazar/gb/data/metricUsers.txt")

  rn_users.foreach(u => fw1.write(u.toString + "\n"))

  fw1.close()

  println(rn_users.length)


}
