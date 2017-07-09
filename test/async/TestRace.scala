package async

import model.{Index, Page}
import scala.collection.JavaConverters._
import org.scalacheck.{Gen, Prop}
import org.scalatest._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.prop._
import scala.concurrent.{Future}

class SetSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {

  val indexSequential = new Index()
  val indexParallel = new Index()

  val examples =
    Table(
      ("page_one", "text for one with examples"),
      ("page_two", "text for two with examples"),
      ("page_three", "text for three with examples"),
      ("page_four", "text for four with examples"),
      ("page_five", "text for five with examples"),
      ("page_six", "text for six with examples"),
      ("page_seven", "text seven one with examples"),
      ("page_eight", "text eight one with examples"),
      ("page_nine", "text nine one with examples"),
      ("page_ten", "text ten one with examples"),

    )

  property("insert sequential ") {
    forAll(examples) { (url, text) =>
      indexSequential.addToIndex(Page(url, text))
    }
  }


  property("insert parallel ") {
    forAll(examples) { (url, text) =>
      Future {
        indexParallel.addToIndex(Page(url, text))
      }
    }
  }

  property("parallel and sequential are equal ") {
    for (key <- indexSequential.getIndex.keys().asScala.toList) {
      val seq = indexSequential.getIndex.get(key)
      val para = indexParallel.getIndex.get(key)
      assert(seq == para)
    }
  }

}



class FailSpec extends PropSpec with PropertyChecks with Matchers {

  val indexSequential = new Index()
  val indexParallel = new Index()
  var count = 0

  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 1000)


  property("insert parallel ") {
    forAll { (url: String, text: String) =>
      count += 1
      indexSequential.addToIndex(Page(url, text))
      Future {
        indexParallel.addToIndex(Page(url, text))
      }
    }
  }

  property("parallel and sequential are equal ") {
    for (key <- indexSequential.getIndex.keys().asScala.toList) {
      val seq = indexSequential.getIndex.get(key)
      val para = indexParallel.getIndex.get(key)
      assert(seq == para)
    }
  }

}

