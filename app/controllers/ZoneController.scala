package controllers

import play.api.mvc.{Controller, Action}
import play.api.libs.json._
import scala.concurrent.Future
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.BSONFormats._
import scala.concurrent.duration._
import scala.concurrent.Await

class ZoneController extends Controller {
  
  import models.{Zone, Project}
  
  def get(projectName: String, zoneName: String) = Action.async {
    val projectBson = Await.result(Project.select(projectName), 5.seconds)
    if (projectBson.isDefined) {
      val project = Project.reader.read(projectBson.get)
      Zone.select(project.id, zoneName).map { result => result match {
        case Some(zone) => Ok(BSONFormats.BSONDocumentFormat.writes(zone).as[JsObject])
        case None => BadRequest("Failed to find zone with name '" + zoneName + "' under project with name '" + projectName + "'")
      }}
    } else {
      Future(BadRequest("Error: failed to find project with name '" + projectName + "'"))
    }
  }
  
  def list(projectName: String) = Action.async {
    val projectBson = Await.result(Project.select(projectName), 5.seconds)
    if (projectBson.isDefined) {
      val project = Project.reader.read(projectBson.get)
      var jsonArray: JsArray = null
      Zone.selectall(project.id).map { results => 
          results.foreach { result => 
            if (jsonArray == null)
              jsonArray = Json.arr(result)
            else
              jsonArray = jsonArray ++ Json.arr(BSONFormats.BSONDocumentFormat.writes(result).as[JsObject])
          }
        if (jsonArray != null)
          Ok(jsonArray) 
        else
          BadRequest("Failed to find zone list under project with name '" + projectName + "'")
      }
    } else {
      Future(BadRequest("Error: failed to find project with name '" + projectName + "'"))
    }
  }
  
}