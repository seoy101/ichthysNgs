package models

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import slick.dbio
import slick.dbio.Effect.Read
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.sys.process._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration
import scala.util.{Failure, Success}
import scala.concurrent.duration.Duration
import play.api.cache._
import java.util.Date

case class Image(pIndex:Int, imgName:String, jobName:String, jobType:String, parentInfo:String, status:String, date:Date, uId:String)

class ImageModel @Inject()(protected val dbConfigProvider: DatabaseConfigProvider){
  
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  
  import dbConfig.driver.api._

  class ImageTable(tag: Tag) extends Table[Image](tag, "Image") {

    def pIndex = column[Int]("pIndex", O.AutoInc, O.PrimaryKey)
    def imgName = column[String]("imgName")
    def jobName = column[String]("jobName")
    def jobType = column[String]("jobType")
    def parentInfo = column[String]("parentInfo")
    def status = column[String]("status")
    def date = column[Date]("date")
    def uId = column[String]("uId")

//    implicit val JavaUtilDateMapper = MappedColumnType .base[java.util.Date, java.sql.Timestamp] (d => new java.sql.Timestamp(d.getTime),d => new java.util.Date(d.getTime))
    
    implicit val utilDate2SqlDate = MappedColumnType.base[java.util.Date, java.sql.Date](
{ utilDate => new java.sql.Date(utilDate.getTime()) },
{ sqlDate => new java.util.Date(sqlDate.getTime()) })
    
    
    def * = (pIndex, imgName, jobName, jobType, parentInfo, status, date ,uId) <> (Image.tupled, Image.unapply)
  }
    val Images = TableQuery[ImageTable]
  
    def insert(imageData:Image)=db.run(DBIO.seq( Images += imageData ))

    def delete(pIndex: Int)= db.run(Images.filter{_.pIndex === pIndex}.result)
    
    def retrieve(imageData: Image):Image= {
      val dbRetrieve = db.run(compiledCheck(imageData.pIndex).result)
      val result =Await.result(dbRetrieve, Duration.Inf)
      if(!result.isEmpty)
        return result(0)
      else
        return null
    }
    def update(imageData: Image)={

    }
    
    def check(pIndex:Rep[Int]) = Images.filter{image => image.pIndex === pIndex}
    def compiledCheck = Compiled(check _) 
}