package controllers

import javax.inject.Inject

import akka.util.ByteString
import model.{Index, InsertUrl, Search, SearchQuery}
import play.api.data._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.http.HttpEntity
import play.api.mvc._



class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {


  val testIndex = new Index()

  val rankSearch = new Search()

  def echo = Action {
    Ok("hello")
  }

  def indexx = Action {
    Result(
      header = ResponseHeader(200, Map.empty),
      body = HttpEntity.Strict(ByteString("Hello world!"), Some("text/plain"))
    )

  }

  def search = Action { implicit request =>

    val searchForm:Form[SearchQuery] = Form(
      mapping(
        "query" -> of[String]
      )(SearchQuery.apply)(SearchQuery.unapply)
    )

    searchForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.search(formWithErrors))
      },
      query => {
        val result = rankSearch.search(query, testIndex.getIndex)
        Ok(views.html.result(result.result))
      }
    )
  }




  def insert = Action { implicit request =>

    val insertForm:Form[InsertUrl] = Form(
      mapping(
        "url" -> of[String]
      )(InsertUrl.apply)(InsertUrl.unapply)
    )

    insertForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.insert(formWithErrors, ""))
      },
      query => {
        val msg = testIndex.addUrl(List(query.url))
        Ok(views.html.insert(insertForm, msg))
      }
    )
  }

  def result = Action { implicit request =>
    Ok(views.html.result(List()))
  }


}
