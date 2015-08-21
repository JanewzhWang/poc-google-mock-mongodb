package initialize

import scala.concurrent.duration._
import scala.concurrent.Await
import java.util.Date
import reactivemongo.bson.BSONObjectID
import models._

object MockInitializer {
  
  def initializeProjects = {
    Await.result(Project.insert(Project(None, Some("pocproj1"), Some("poc project 1"))), 5.seconds)
    Await.result(Project.insert(Project(None, Some("pocproj2"), Some("poc project 2"))), 5.seconds)
    Await.result(Project.insert(Project(None, Some("pocproj3"), Some("poc project 3"))), 5.seconds)
  }
  
  def initializeRegions = {
    var projectId: Option[BSONObjectID] = None
    val projectBson = Await.result(Project.select("pocproj1"), 5.seconds)
    if (projectBson.isDefined)
      projectId = Project.reader.read(projectBson.get).id
    Await.result(Region.insert(Region(None, 
          Some(new Date), 
          Some("asia-east1"), 
          Some("asia-east1"), 
          Some("UP"), 
          Some(List(Quota(Some("quota-metric1"), Some(88.82), Some(0.88)), Quota(Some("quota-metric2"), Some(0.98), Some(0.98)))),
          Some(Deprecated(Some("DELETED"), None, None, None, Some(new Date))),
          projectId)), 5.seconds)
  }
  
  def initializeZones = {
    var project: Option[Project] = None
    val projectBson = Await.result(Project.select("pocproj1"), 5.seconds)
    if (projectBson.isDefined)
      project = Some(Project.reader.read(projectBson.get))
    var region: Option[Region] = None
    val regionBson = Await.result(Region.select(project.get.id, "asia-east1"), 5.seconds)
    if (regionBson.isDefined)
      region = Some(Region.reader.read(regionBson.get))
    Await.result(Zone.insert(
        Zone(None,
          Some(new Date),  //creationTimestamp
          Some("asia-east1-a"),  //name
          Some("asia-east1-a"),  //description
          Some("DOWN"),          //status
          region.get.id,        //regionId
          project.get.id,        //projectId
          Some(List(MaintenanceWindow(
              Some("backup"), 
              Some("backup time window"), 
              Some(new Date), 
              Some(new Date)))),
          Some(Deprecated(                //deprecated
              Some("DELETED"),
              None,
              None,
              None,
              Some(new Date))))), 5.seconds)
  }
  
}