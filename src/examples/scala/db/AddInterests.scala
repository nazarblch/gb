package db

import load.{TopicsTable, GoogleTable}

object AddInterests extends App {

//  val table = new GoogleTable()
//
//  table.interestsNames.toVector.sortBy(_._2).foreach({case(name, id) =>
//    val cat = table.getCat(name)
//    val title = table.getTitle(name)
//    val interest = new DBInterest(id, title, Set(), cat)
//    InterestsModel.addInterest(interest)
//  })

  val topict = new TopicsTable("/Users/buzun/Downloads/kw6.xlsx")

  topict.activeCols.foreach(col => {
    val kw = topict.getWords(col)
    val name = topict.headerRow(col)
    InterestsModel.addKW(name, Set())
    println(name, kw)
    val obj = InterestsModel.getInterestByStr(name)
    println(obj)
    InterestsModel.addKW(name, kw)
  })




}
