package model

import java.util.concurrent.ConcurrentHashMap

import org.jsoup.Jsoup

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.Set


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
    * @return pages as ranked list.
    */
  def search(query: SearchQuery, index: ConcurrentHashMap[String, Set[Page]]) : SearchResult = {
    val words = query.query.split(" ").toList
    val found = searchForAll(words, index)
    val ranked = makeRanking(found).take(1000)
    val result = ranked.map(page => buildResultItem(page, words))
    SearchResult(query.query, result)
  }


  /**
    * Return for any word the pages in the index. If no value in the index, then it gives back an
    * empty set.
    * @param words
    * @return a list of sets of urls. Any set of urls belong to one word.
    */
  def searchForAll(words: List[String], index: ConcurrentHashMap[String, Set[Page]]):List[Set[Page]] =
    words.map(w => searchForSingleWord(w, index))


  def searchForSingleWord(word: String, index: ConcurrentHashMap[String, Set[Page]]): Set[Page] =
    if (index.containsKey(word)) {
      val test = index.get(word)
      test
    }  else Set.empty[Page]


  def buildResultItem(page: Page, words: List[String]):ResultItem = {
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
  def makeRanking(all: List[Set[Page]]): List[Page] = {
    val counted = all.foldLeft(Map[Page, Int]())((b,a) => count(a, b))
    counted.toList.sortBy(_._2).reverse.map(_._1)
  }

  /**
    * Add any url to the given map.
    * @param ids the urls.
    * @param m map which count how often an url appears.
    * @return a map, which contains the values of the url and how often it appeared.
    */
  def count(ids: Set[Page], m: Map[Page, Int]):Map[Page, Int] = {
    m ++ ids.map(id => if (m.contains(id)) (id, m(id) + 1) else (id, 1)).toMap
  }


}


class Index {


  private var indexMap: ConcurrentHashMap[String, Set[Page]] = new ConcurrentHashMap[String, Set[Page]]()
  private var foundUrls: ConcurrentHashMap[String, Unit] = new ConcurrentHashMap[String, Unit]()

  def getIndex = indexMap
  def getUrls = foundUrls


  /**
    * Add an url and the text for this url to the index. The function works recursiv for all links on this site
    * which connect to subdomains of this side.
    * For example www.test.de would insert www.test.de/news to, if there is a link on this page.
    * @param urls
    * @return a message if the page could be stored successful or not.
    */
  @tailrec
  final def addUrl(urls: List[String]):String = {

    if (urls.isEmpty) {
      return "Url is empty."
    }
    else if (foundUrls.containsKey(urls.head)) {
      addUrl(urls.tail)
    } else {
      val url = urls.head
      var insideLinks = List.empty[String]
      try {
        val document = Jsoup.connect(url).get
        val text = document.text()
          .replace(",","")
          .replace(".", "")
          .replace(":","")
          .replace(";","")
        println("insert: " + url)
        addToIndex(Page(url, text))
        val links = document.select("a").asScala.map(f => f.attr("href"))
        insideLinks = links.filter(l => l.startsWith(url) && l != url).toList
      } catch {
        case e: Throwable => {
          e.getMessage
        }
      }
      addUrl(urls.tail ++ insideLinks)
    }
  }


  /**
    * Add page to index and internal map.
    * @param page
    * @return A new changed index.
    */
  def addToIndex(page: Page) = {

    val words = page.text.split(" ")
    words.foreach(w => addSingleWord(w, page))

  }

  def addSingleWord(word: String, page: Page): Unit = {
    this.synchronized {
      if (indexMap.containsKey(word)) {
        indexMap.get(word).add(page)
      } else {
        val pageSet: mutable.Set[Page] = Set(page)
        indexMap.put(word, pageSet)
      }
      foundUrls.put(page.url, ())
    }
  }



}




