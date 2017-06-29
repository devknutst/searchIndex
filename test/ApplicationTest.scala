import model.{Index, Page, Search, SearchQuery}
import org.scalatestplus.play.PlaySpec


class ApplicationTest extends PlaySpec {


  val index = new Index()

  val search = new Search()


  "A index must " must {
    "accept new pages." in {
      val pageOne = new Page("pageOne", "Text for page one.")
      val pageTwo = new Page("pageTwo", "Text for page two.")
      index.addToIndex(pageOne)
      index.addToIndex(pageTwo)
      index.getIndex size() mustBe 5
    }

    "more pages for an already existing word." in {
      val pageThree = new Page("pageThree", "Text for page three.")
      index.addToIndex(pageThree)
      index.getIndex.get("Text").size mustBe 3
    }

    "have only one page for a word that exits only in this page." in {
      val pageFour = new Page("pageFour", "Text for page four.")
      index.addToIndex(pageFour)
      index.getIndex.get("four.").size mustBe 1
    }
  }


  "The index url scraper " must {
    " show an error message for an non existing url." in {
      index.addUrl(List("xyz")) mustEqual "Url is empty."
    }
  }

  "A search over this index " must {

    " find pages which contains a search word. " in {
      search.search(SearchQuery("Text"), index.getIndex).result.size mustBe 4
    }

    " show an empty result for a non existing word." in {
      search.search(SearchQuery("abc"), index.getIndex).result.size mustBe 0
    }

    " show a correct ranking if the search query contains more than one word. " in {
      val result = search.search(SearchQuery("Text one."), index.getIndex)

      result.result.head.url mustBe "pageOne"
      result.result.tail.head.url must not be "pageOne"

    }
  }



}
