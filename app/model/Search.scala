package model

import org.jsoup.Jsoup

import scala.collection.JavaConverters._

case class Page(url: String, text: String)
case class SearchQuery(query: String)
case class ResultItem(url: String, text: String)
case class SearchResult(query: String, result: List[ResultItem])
case class InsertUrl(url: String)



class Index {


  private var index = Map[String, Set[String]]()

  private var pageMap = Map[String, Page]()


  def search(query: SearchQuery): SearchResult = {
    val words = query.query.split(" ").toList
    val found = searchForAll(words)
    val ranked = makeRanking(found).take(1000)
    val result = ranked.map(id => buildResultItem(id, words))
    SearchResult(query.query, result)
  }


  def searchForAll(words: List[String]):List[Set[String]] = words.map(w => searchForSingleWord(w))


  def searchForSingleWord(word: String): Set[String] = index.getOrElse(word, Set[String]())


  def buildResultItem(id: String, words: List[String]):ResultItem = {
    val page = pageMap(id)
    val text = words.map(w => findTextForWord(w, page.text)) mkString "..."
    ResultItem(page.url, text)
  }


  def findTextForWord(word: String, text: String):String = {
    val index = text.indexOf(word)
    if (index > -1) {
      val range = 15
      val begin = if (index < range) 0 else index - range
      val end = if (index + range >= text.length) text.length else (index + range)
      text.substring(begin, end)
    } else {
      ""
    }
  }


  def makeRanking(all: List[Set[String]]): List[String] = {
    val counted = all.foldLeft(Map[String, Int]())((b,a) => count(a, b))
    counted.toList.sortBy(_._2).reverse.map(_._1)
  }

  def count(ids: Set[String], m: Map[String, Int]):Map[String, Int] = {
    m ++ ids.map(id => if (m.contains(id)) (id, m(id) + 1) else (id, 1)).toMap
  }


  def addUrl(url: String):String = {
    if (pageMap.contains(url)) "Url already exists."
    else {
      try {
        val document = Jsoup.connect(url).get
        val text = document.body().text()
          .replace(",","")
          .replace(".", "")
          .replace(":","")
          .replace(";","")
        addToIndex(Page(url, text))
        val links = document.select("a").asScala.map(f => f.attr("href"))
        val insideLinks = links.filter(l => l.startsWith(url))
        for (l <- insideLinks) {
          println("link intern" + l)
          addUrl(l)
        }
        s"Side $url successfull scraped and stored."
      } catch {
        case e: Exception => e.getLocalizedMessage
      }
    }
  }


  def addToIndex(page: Page) = {
    pageMap = pageMap + (page.url -> page)
    index = index ++ changeIndex(page)
    index
  }


  def changeIndex(page: Page):Map[String, Set[String]] = {
    val words = page.text.split(" ")
    words.map(w => getUrls(w, page.url)).toMap
  }


  def getUrls(word: String, url: String):(String,Set[String]) = {
    val set = if (index.contains(word)) {
      index(word)
    } else {
      Set[String]()
    }
    (word, set + url)
  }

}




