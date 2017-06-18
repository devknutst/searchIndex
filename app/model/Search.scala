package model

import org.jsoup.Jsoup

import scala.collection.JavaConverters._

case class Page(url: String, text: String)
case class SearchQuery(query: String)
case class ResultItem(url: String, text: String)
case class SearchResult(query: String, result: List[ResultItem])
case class InsertUrl(url: String)




class Search {


  /**
    * Search for given words and give back the pages in a ranked list.
    * @param query search words
    * @param index index
    * @param pageMap map for pages.
    * @return pages as ranked list.
    */
  def search(query: SearchQuery, index: Map[String, Set[String]], pageMap: Map[String, Page]) : SearchResult = {
    val words = query.query.split(" ").toList
    val found = searchForAll(words, index)
    val ranked = makeRanking(found).take(1000)
    val result = ranked.map(id => buildResultItem(id, words, pageMap))
    SearchResult(query.query, result)
  }


  /**
    * Return for any word the pages in the index. If no value in the index, then it gives back an
    * empty set.
    * @param words
    * @return a list of sets of urls. Any set of urls belong to one word.
    */
  def searchForAll(words: List[String], index: Map[String, Set[String]]):List[Set[String]] =
    words.map(w => searchForSingleWord(w, index))


  def searchForSingleWord(word: String, index: Map[String, Set[String]]): Set[String] =
    index.getOrElse(word, Set[String]())


  def buildResultItem(id: String, words: List[String], pageMap: Map[String, Page]):ResultItem = {
    val page = pageMap(id)
    val text = words.map(w => findTextForWord(w, page.text)) mkString "..."
    ResultItem(page.url, text)
  }

  /**
    * Makes an extraction for any word in the complete text of the page.
    * @param word search word.
    * @param text complete text
    * @return extraction or if the word not found, an empty string.
    */
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

  /**
    * Makes an ranking for the found urls. An url can be in more the one of the given sets. It will be counted, how often
    * an url appears in the different sets. As more often it appears in the sets, as higher it will be climbing in
    * the ranking.
    * @param all urls in different sets.
    * @return the urls in a ranking order as list.
    */
  def makeRanking(all: List[Set[String]]): List[String] = {
    val counted = all.foldLeft(Map[String, Int]())((b,a) => count(a, b))
    counted.toList.sortBy(_._2).reverse.map(_._1)
  }

  /**
    * Add any url to the given map.
    * @param ids the urls.
    * @param m map which count how often an url appears.
    * @return a map, which contains the values of the url and how often it appeared.
    */
  def count(ids: Set[String], m: Map[String, Int]):Map[String, Int] = {
    m ++ ids.map(id => if (m.contains(id)) (id, m(id) + 1) else (id, 1)).toMap
  }



}


class Index {


  private var index = Map[String, Set[String]]()

  private var pageMap = Map[String, Page]()


  def getIndex = index

  def getPageMap = pageMap


  /**
    * Add any url to the given map.
    * @param ids the urls.
    * @param m map which count how often an url appears.
    * @return a map, which contains the values of the url and how often it appeared.
    */
  def count(ids: Set[String], m: Map[String, Int]):Map[String, Int] = {
    m ++ ids.map(id => if (m.contains(id)) (id, m(id) + 1) else (id, 1)).toMap
  }


  /**
    * Add an url and the text for this url to the index. The function works recursiv for all links on this site
    * which connect to subdomains of this side.
    * For example www.test.de would insert www.test.de/news to, if there is a link on this page.
    * @param url
    * @return a message if the page could be stored successful or not.
    */
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


  /**
    * Add page to index and internal map.
    * @param page
    * @return A new changed index.
    */
  def addToIndex(page: Page) = {
    pageMap = pageMap + (page.url -> page)
    index = index ++ changeIndex(page)
    index
  }


  /**
    * Add page to index and give back an changed index.
    * @param page
    * @return the changed index.
    */
  def changeIndex(page: Page):Map[String, Set[String]] = {
    val words = page.text.split(" ")
    words.map(w => getUrls(w, page.url)).toMap
  }


  /**
    * Returns the urls for a given word, including the new url, which is given to the function.
    * @param word
    * @param url new url.
    * @return the word and all urls for this word.
    */
  def getUrls(word: String, url: String):(String,Set[String]) = {
    val set = if (index.contains(word)) {
      index(word)
    } else {
      Set[String]()
    }
    (word, set + url)
  }

}




