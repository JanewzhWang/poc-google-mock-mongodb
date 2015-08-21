package models

import java.util.Date
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.text.SimpleDateFormat
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter}
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONString


case class Deprecated(
    state: Option[String],
    replacement: Option[Date],
    deprecated: Option[Date],
    obsolete: Option[Date],
    deleted: Option[Date])
    
object Deprecated {
  
  implicit object DeprecatedReader extends BSONDocumentReader[Deprecated] {
    def read(bson: BSONDocument): Deprecated = {
      val deprecated = Deprecated(
          bson.getAs[String]("deprecated"),
          bson.getAs[String]("replacement").map { creationTimestamp => 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(creationTimestamp)
          },
          bson.getAs[String]("deprecated").map { creationTimestamp => 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(creationTimestamp)
          },
          bson.getAs[String]("obsolete").map { creationTimestamp => 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(creationTimestamp)
          },
          bson.getAs[String]("deleted").map { creationTimestamp => 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(creationTimestamp)
          })
      deprecated
    }
  }
  
  implicit object DeprecatedWriter extends BSONDocumentWriter[Deprecated] {
    def write(deprecated: Deprecated): BSONDocument = {
      var rep: Option[BSONString] = None
      if (deprecated.replacement.isDefined)
        rep = Some(BSONString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(deprecated.replacement.get)))
      var dep: Option[BSONString] = None
      if (deprecated.deprecated.isDefined)
        dep = Some(BSONString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(deprecated.deprecated.get)))
      var obs: Option[BSONString] = None
      if (deprecated.obsolete.isDefined)
        obs = Some(BSONString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(deprecated.obsolete.get)))
      var del: Option[BSONString] = None
      if (deprecated.deleted.isDefined)
        del = Some(BSONString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(deprecated.deleted.get)))
      BSONDocument("state" -> deprecated.state,
          "replacement" -> rep,
          "deprecated" -> dep,
          "obsolete" -> obs,
          "deleted" -> del)
    }
  }
}