package controllers
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.Helpers.{GET => GET_REQUEST, _}
import play.api.test._

import scala.concurrent.Future

/**
  * Created by knut on 04.07.17.
  */
class ControllerTest extends PlaySpec with Results {


  "Test function indexx " should {
    "should be valid" in {
      val controller = new Application(stubControllerComponents())
      val result: Future[Result] = controller.indexx().apply(FakeRequest())
      controller.insert
      val bodyText: String = contentAsString(result)
      bodyText mustBe "Hello world!"
    }
  }

  "Application  controller " should {
    "url should open side for input " in {

      val request = FakeRequest()
      val controller = new Application(stubControllerComponents())
      val result: Future[Result] = controller.insert().apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText must endWith ("</html>")
    }

  }

}


class ExampleSpec extends PlaySpec with GuiceOneAppPerSuite {

  // Override fakeApplication if you need a Application with other than
  // default parameters.
  override def fakeApplication() = new GuiceApplicationBuilder().configure(Map("ehcacheplugin" -> "disabled")).build()

  "The GuiceOneAppPerSuite trait" must {
    "provide an Application" in {
      app.configuration.getOptional[String]("ehcacheplugin") mustBe Some("disabled")
    }
    "start the Application" in {
      Play.maybeApplication mustBe Some(app)
    }
  }
}


