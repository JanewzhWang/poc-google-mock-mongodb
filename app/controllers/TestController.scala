package controllers

import javax.inject.Inject
import play.api.mvc.Controller
import play.modules.reactivemongo._
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.api._
import reactivemongo.bson.BSONDocument

//case class Project(projectId: Long, projectName: String)

//object Project {
//  
//  import scala.concurrent.Future
//
//  import play.api.Play.current
//  import play.api.libs.concurrent.Execution.Implicits.defaultContext
//  
//  import play.modules.reactivemongo.ReactiveMongoApi
//  import play.modules.reactivemongo.json._
//  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]
//  
//}

class TestController @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {
  
  import play.modules.reactivemongo.json._
  import play.modules.reactivemongo.json.collection.JSONCollection
  
  def collection = db.collection[JSONCollection]("projects")
  
  def save(projectId: Long, projectName: String) = Action.async {
    collection.insert(Json.obj("project_id" -> projectId, "project_name" -> projectName)).map{
      lastError =>
        Ok("Mongo LastError: %s".format(lastError))
    }
  }
  
  def queryCriteria(aboveId: Long) = Action.async {
    val selector = BSONDocument("project_id" -> BSONDocument("$gt" -> aboveId))
    val cursor: Cursor[JsObject] = collection.find(selector).cursor[JsObject]
    val futureProjectsList = cursor.collect[List]()
    val futureProjectsJsonArray = futureProjectsList.map{ projects => Json.arr(projects) }
    futureProjectsJsonArray.map { projects => Ok(projects) }
  }
  
  def find(projectId: Long) = Action.async {
    val cursor: Cursor[JsObject] = collection.find(Json.obj("project_id" -> projectId)).cursor[JsObject]
    val futureProjectsList = cursor.collect[List]()
    val futureProjectsJsonArray = futureProjectsList.map{ projects => JsArray(projects) }
    futureProjectsJsonArray.map { projects => Ok(projects) }
  }
  
}