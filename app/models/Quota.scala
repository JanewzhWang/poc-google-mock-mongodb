package models

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter}
import reactivemongo.bson.BSONDocument

case class Quota (
    metric: Option[String],
    limit: Option[Double],
    usage: Option[Double])
    
object Quota {
  
  implicit object QuotaReader extends BSONDocumentReader[Quota] {
    def read(bson: BSONDocument): Quota = {
      Quota(bson.getAs[String]("metric"),
          bson.getAs[Double]("limit"),
          bson.getAs[Double]("usage"))
    }
  }
  
  implicit object QuotaWriter extends BSONDocumentWriter[Quota] {
    def write(quota: Quota): BSONDocument = {
      BSONDocument("metric" -> quota.metric,
          "limit" -> quota.limit,
          "usage" -> quota.usage)
    }
  }
  
}