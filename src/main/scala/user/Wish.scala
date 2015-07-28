package user

class Wish private (val id: Int, val name: String) {
  override def toString = id + " " + name
}

object Wish {
  def apply(id: Int, name: String): Wish = {
    new Wish(id, name.replaceAll("[^a-zA-Zа-яА-Я_0-9]", " ").trim.split(" ").map(_.toLowerCase).mkString(" "))
  }
}
