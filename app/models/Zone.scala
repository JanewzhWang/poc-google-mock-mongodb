package models

import java.util.Date
import play.api.libs.json._
import scala.annotation.meta.getter
import java.text.SimpleDateFormat
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import reactivemongo.api.commands.WriteResult
import play.api.libs.functional.syntax._
import play.modules.reactivemongo._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.duration._

case class Zone(
    override val id: Option[BSONObjectID],
    creationTimestamp: Option[Date],
    name: Option[String],
    description: Option[String],
    status: Option[String],
    regionId: Option[BSONObjectID],
    projectId: Option[BSONObjectID],
    maintenanceWindows: Option[List[MaintenanceWindow]],
    deprecated: Option[Deprecated]) extends BaseModel(id) {
  var region: Option[Region] = None
  var project: Option[Project] = None
}

object Zone extends BaseModelOperations[Zone] {
  
  override def getTableName: String = "zones"
  
  /*
   * initialize reader and writer
   */
  {
    super.setReader(ZoneReader)
    super.setWriter(ZoneWriter)
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
  
  def selectByRegionId(projectId: Option[BSONObjectID], regionId: Option[BSONObjectID]): Future[List[BSONDocument]] = {
    selectMultiple(BSONDocument(
        "regionId" -> BSONDocument("$eq" -> regionId), 
        "projectId" -> BSONDocument("$eq" -> projectId)))
  }
  
}

object ZoneReader extends BSONDocumentReader[Zone] {
    def read(bson: BSONDocument): Zone = {
      val zone = Zone(
          bson.getAs[BSONObjectID]("_id"), 
          bson.getAs[String]("creationTimestamp").map { creationTimestamp => 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(creationTimestamp)
          },
          bson.getAs[String]("name"), 
          bson.getAs[String]("description"),
          bson.getAs[String]("status"),
          bson.getAs[BSONObjectID]("regionId"),
          bson.getAs[BSONObjectID]("projectId"),
          bson.getAs[List[MaintenanceWindow]]("maintenanceWindows"),
          bson.getAs[Deprecated]("deprecated"))
      val zoneBson = Await.result(Region.select(zone.regionId), 5.seconds)
      if (zoneBson.isDefined)
        zone.region = Some(Region.reader.read(zoneBson.get))
      val projectBson = Await.result(Project.select(zone.projectId), 5.seconds)
      if (projectBson.isDefined)
        zone.project = Some(Project.reader.read(projectBson.get))
      zone
    }
  }  
  
object ZoneWriter extends BSONDocumentWriter[Zone] {
  def write(zone: Zone): BSONDocument = {
    var regionId: Option[BSONObjectID] = zone.regionId
    if (!regionId.isDefined && zone.region.isDefined)
      regionId = zone.region.get.id
    var projectId: Option[BSONObjectID] = zone.projectId
    if (!projectId.isDefined && zone.project.isDefined)
      projectId = zone.project.get.id
    var projectName: Option[String] = None
    if (projectId.isDefined) {
      val projectBson = Await.result(Project.select(projectId), 5.seconds)
      if (projectBson.isDefined)
        projectName = Project.reader.read(projectBson.get).name
    }
    BSONDocument(
        "selfLink" -> BSONString("https://www.googleapis.com/compute/v1/projects/" + projectName.get + "/zones/" + zone.name.get),
        "kind" -> BSONString("compute#zone"),
        "regionId" -> regionId,
        "projectId" -> projectId, 
        "creationTimestamp" -> BSONString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(zone.creationTimestamp.get)),
        "id" -> zone.id,
        "name" -> zone.name,
        "description" -> zone.description,
        "status" -> zone.status,
        "maintenanceWindows" -> zone.maintenanceWindows,
        "deprecated" -> zone.deprecated)
  }
}

