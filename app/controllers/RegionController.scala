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

class RegionController extends Controller {
  
  import models.{Region, Project}
  
  def get(projectName: String, regionName: String) = Action.async {
    val projectBson = Await.result(Project.select(projectName), 5.seconds)
    if (projectBson.isDefined) {
      val project = Project.reader.read(projectBson.get)
      models.Region.select(project.id, regionName).map { result => result match {
        case Some(region) => Ok(BSONFormats.BSONDocumentFormat.writes(region).as[JsObject])
        case None => BadRequest("Failed to find region with name '" + regionName + "' under project with name '" + projectName + "'")
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
      Region.selectall(project.id).map { results => 
          results.foreach { result => 
            if (jsonArray == null)
              jsonArray = Json.arr(result)
            else
              jsonArray = jsonArray ++ Json.arr(BSONFormats.BSONDocumentFormat.writes(result).as[JsObject])
          }
        if (jsonArray != null)  
          Ok(jsonArray)
        else
          BadRequest("Failed to find region list under project with name '" + projectName + "'")
      }
    } else {
      Future(BadRequest("Error: failed to find project with name '" + projectName + "'"))
    }
  }
  
}