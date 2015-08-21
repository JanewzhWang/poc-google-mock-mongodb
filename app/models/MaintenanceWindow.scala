package models

import java.util.Date
import play.api.libs.json.Json
import play.api.libs.json.Writes
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter}
import reactivemongo.bson.BSONDocument
import java.text.SimpleDateFormat
import reactivemongo.bson.BSONString

case class MaintenanceWindow(
    name: Option[String],
    description: Option[String],
    beginTime: Option[Date],
    endTime: Option[Date])
    
object MaintenanceWindow {
  
  implicit object MaintenanceWindowReader extends BSONDocumentReader[MaintenanceWindow] {
    def read(bson: BSONDocument): MaintenanceWindow = {
      val maintenanceWindow = MaintenanceWindow(
          bson.getAs[String]("name"),
          bson.getAs[String]("description"),
          bson.getAs[String]("beginTime").map { creationTimestamp => 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(creationTimestamp)
          },
          bson.getAs[String]("endTime").map { creationTimestamp => 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(creationTimestamp)
          })
      maintenanceWindow
    }
  }
  
  implicit object MaintenanceWindowWriter extends BSONDocumentWriter[MaintenanceWindow] {
    def write(maintenanceWindow: MaintenanceWindow): BSONDocument = {
      BSONDocument(
          "name" -> maintenanceWindow.name,
          "description" -> maintenanceWindow.description,
          "beginTime" -> maintenanceWindow.beginTime.map { mwb => 
            BSONString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(mwb)) },
          "endTime" -> maintenanceWindow.endTime.map { mwe =>  
            BSONString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(mwe))})
    }
  }
}