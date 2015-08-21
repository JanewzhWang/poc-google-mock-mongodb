package controllers

import play.api.mvc.{Controller, Action}
import play.api.libs.json._
import scala.concurrent.Future
import models.Project
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.BSONFormats._

class ProjectController extends Controller {
  
  def get(name: String) = Action.async {
    Project.select(name).map { result => result match {
      case Some(project) => Ok(BSONFormats.BSONDocumentFormat.writes(project).as[JsObject])
      case None => BadRequest("Failed to find specific project with name '" + name + "'")
    }}
  }
  
  def list = Action.async {
    var jsonArray: JsArray = null
    Project.selectall.map { results => 
        results.foreach { result => 
          if (jsonArray == null)
            jsonArray = Json.arr(result)
          else
            jsonArray = jsonArray ++ Json.arr(BSONFormats.BSONDocumentFormat.writes(result).as[JsObject])
        }
      if (jsonArray != null)
        Ok(jsonArray) 
      else
        BadRequest("Failed to find project list")
    }
  }
  
}