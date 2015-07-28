package user

class Group(val name: String, val text: String) {
  def getShortText(wc: Int) = name + " " + text.split(" ").take(wc).mkString(" ")
}
