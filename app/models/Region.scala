package models

import java.util.Date
import scala.annotation.meta.getter
import java.text.SimpleDateFormat
import play.api.libs.json._
import scala.concurrent.{Future, Await}
import scala.util.{Failure, Success}
import reactivemongo.api.commands.WriteResult
import play.api.libs.functional.syntax._
import play.modules.reactivemongo._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.duration._

case class Region(
    override val id: Option[BSONObjectID],
    creationTimestamp: Option[Date],
    name: Option[String],
    description: Option[String],
    status: Option[String],
    quotas: Option[List[Quota]],
    deprecated: Option[Deprecated],
    projectId: Option[BSONObjectID]) extends BaseModel(id) {
  var zones: Option[List[Zone]] = None
  var project: Option[Project] = None
}

object Region extends BaseModelOperations[Region] {
  
  override def getTableName: String = "regions"
  
  /*
   * initialize reader and writer
   */
  {
    super.setReader(RegionReader)
    super.setWriter(RegionWriter)
  }
  
  def select(projectId: Option[BSONObjectID], name: String): Future[Option[BSONDocument]] = {
    selectOne(BSONDocument(
        "name" -> BSONDocument("$eq" -> name.trim), 
        "projectId" -> BSONDocument("$eq" -> projectId)))
  }
  
  def selectall(projectId: Option[BSONObjectID]): Future[List[BSONDocument]] = {
    selectMultiple(BSONDocument(
        "projectId" -> BSONDocument("$eq" -> projectId)))
  }
  
}

object RegionReader extends BSONDocumentReader[Region] {
    def read(bson: BSONDocument): Region = {
      val region = Region(
          bson.getAs[BSONObjectID]("_id"), 
          bson.getAs[String]("creationTimestamp").map { creationTimestamp => 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(creationTimestamp)
          },
          bson.getAs[String]("name"), 
          bson.getAs[String]("description"),
          bson.getAs[String]("status"),
          bson.getAs[List[Quota]]("quotas"),
          bson.getAs[Deprecated]("deprecated"),
          bson.getAs[BSONObjectID]("projectId"))
      val projectBson = Await.result(Project.select(region.projectId), 5.seconds)
      if (projectBson.isDefined)
        region.project = Some(Project.reader.read(projectBson.get))
      region.zones = Some(Await.result(
          Zone.selectByRegionId(region.project.get.id, region.id), 5.seconds).map { zone => 
        Zone.reader.read(zone) })
      region
    }
  }  
  
object RegionWriter extends BSONDocumentWriter[Region] {
  def write(region: Region): BSONDocument = {
    var projectId: Option[BSONObjectID] = region.projectId
    if (!projectId.isDefined && region.project.isDefined)
      projectId = region.project.get.id
    var projectName: Option[String] = None
    if (projectId.isDefined) {
      val projectBson = Await.result(Project.select(region.projectId), 5.seconds)
      if (projectBson.isDefined)
        projectName = Project.reader.read(projectBson.get).name
    }
    println("RegionWriter: projectId=" + projectId.get.stringify + ", projectName=" + projectName)
      BSONDocument(
        "selfLink" -> BSONString("https://www.googleapis.com/compute/v1/projects/" + projectName.get + "/regions/" + region.name.get),
        "kind" -> BSONString("compute#region"),
        "projectId" -> projectId, 
        "creationTimestamp" -> BSONString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(region.creationTimestamp.get)),
        "id" -> region.id,
        "name" -> region.name,
        "description" -> region.description,
        "status" -> region.status,
        "quotas" -> region.quotas,
        "deprecated" -> region.deprecated)
  }
}