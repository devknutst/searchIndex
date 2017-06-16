package controllers

import model.{Index, InsertUrl, SearchQuery}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._

object Application extends Controller {


  val index = new Index()



  def search = Action { implicit  request =>

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
        val result = index.search(query)
        Ok(views.html.main(result.result))
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
        val msg = index.addUrl(query.url)
        Ok(views.html.insert(insertForm, msg))
      }
    )
  }



  def result = Action { implicit request =>
    Ok(views.html.main(List()))
  }


}
