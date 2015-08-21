package models

import play.api.libs.functional.syntax._
import play.modules.reactivemongo._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import reactivemongo.api.commands.WriteResult

class BaseModel(val id: Option[BSONObjectID])

trait ReadWrite[T] {
  
  implicit var reader: BSONDocumentReader[T] = null
  implicit var writer: BSONDocumentWriter[T] = null
  
  protected def setReader(reader: BSONDocumentReader[T]) {
    this.reader = reader
  }
  
  protected def setWriter(writer: BSONDocumentWriter[T]) {
    this.writer = writer
  }
  
}

trait BaseModelOperations[T <: BaseModel] extends ReadWrite[T]  {
  
  protected def getTableName: String
  
  private lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]
  protected def collection = reactiveMongoApi.db.collection[BSONCollection](getTableName)
  
  def insert(obj: T): Future[WriteResult] = {
    collection.insert(obj)
  }
  
  def update(obj: T) = {
    val selector = BSONDocument("_id" -> BSONDocument("$eq" -> obj.id))
    collection.update(selector, obj).onComplete { 
      case Failure(e) => throw e
      case Success(lastError) => println("successfully update object: " + lastError) }
  }
  
  def select(id: Option[BSONObjectID]): Future[Option[BSONDocument]] = {
    val selector = BSONDocument("_id" -> BSONDocument("$eq" -> id))
    collection.find(selector).one[BSONDocument]
  }
  
  def selectOne(selector: BSONDocument): Future[Option[BSONDocument]] = {
    collection.find(selector).cursor[BSONDocument].headOption
  }
  
  def selectMultiple(selector: BSONDocument): Future[List[BSONDocument]] = {
    collection.find(selector).cursor[BSONDocument].collect[List]()
  }
  
  def selectall: Future[List[BSONDocument]] = {
    collection.find(BSONDocument()).cursor[BSONDocument].collect[List]()
  }
  
}