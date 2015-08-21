package models

import scala.util.{Failure, Success}
import scala.concurrent.Future
import play.api.libs.iteratee.Iteratee
import reactivemongo.api.commands.WriteResult
import play.api.libs.functional.syntax._
import play.modules.reactivemongo._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import reactivemongo.api.collections.bson.BSONCollection
import play.modules.reactivemongo.json.BSONFormats.BSONDocumentFormat

case class Project(
    override val id: Option[BSONObjectID],
    name: Option[String],
    description: Option[String]) extends BaseModel(id)

object Project extends BaseModelOperations[Project] {
  
  override def getTableName: String = "projects"
  
  /*
   * initialize reader writer
   */
  {
    super.setReader(ProjectReader)
    super.setWriter(ProjectWriter)
  }
  
  def select(name: String): Future[Option[BSONDocument]] = {
    selectOne(BSONDocument("name" -> BSONDocument("$eq" -> name.trim)))
  }
 
}

object ProjectReader extends BSONDocumentReader[Project] {
  def read(bson: BSONDocument): Project = {
    Project(bson.getAs[BSONObjectID]("_id"), 
        bson.getAs[String]("name"), 
        bson.getAs[String]("description"))
  }
}  

object ProjectWriter extends BSONDocumentWriter[Project] {
  def write(project: Project): BSONDocument = {
    BSONDocument(
        "_id" -> project.id.getOrElse(BSONObjectID.generate),
        "name" -> project.name,
        "description" -> project.description)
  }
}