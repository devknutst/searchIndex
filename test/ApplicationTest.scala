import model.{Index, Page, SearchQuery}
import org.scalatestplus.play.PlaySpec


class ApplicationTest extends PlaySpec {


  val index = new Index()


  "A index must " must {
    "accept new pages." in {
      val pageOne = new Page("pageOne", "Text for page one.")
      val pageTwo = new Page("pageTwo", "Text for page two.")
      index.addToIndex(pageOne)
      index.addToIndex(pageTwo).size mustBe 5
    }

    "more pages for an already existing word." in {
      val pageThree = new Page("pageThree", "Text for page three.")
      index.addToIndex(pageThree)("Text").size mustBe 3
    }

    "have only one page for a word that exits only in this page." in {
      val pageFour = new Page("pageFour", "Text for page four.")
      index.addToIndex(pageFour)("four.").size mustBe 1
    }
  }


  "A index search " must {

    " find pages which contains a search word. " in {
      index.search(SearchQuery("Text")).result.size mustBe 4
    }

    " show an empty result for a non existing word." in {
      index.search(SearchQuery("abc")).result.size mustBe 0
    }

    " show a correct ranking if the search query contains more than one word. " in {
      val result = index.search(SearchQuery("Text one."))

      result.result.head.url mustBe "pageOne"
      result.result.tail.head.url must not be "pageOne"

    }
  }

  "The index url scraper " must {
    " show an error message for an non existing url." in {
      index.addUrl("xyz") mustEqual "Malformed URL: xyz"
    }
  }



}
