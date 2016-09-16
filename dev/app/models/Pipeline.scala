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

case class Pipeline(pIndex:Int, pipeName:String, customPipe:String, uId:String)

class PipelineModel @Inject()(protected val dbConfigProvider: DatabaseConfigProvider){
  
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  
  import dbConfig.driver.api._
  
  class PipelineTable(tag: Tag) extends Table[Pipeline](tag, "Pipeline") {

    def pIndex = column[Int]("pIndex", O.AutoInc, O.PrimaryKey)
    
    def pipeName = column[String]("pipeName")
    def customPipe = column[String]("customPipe")
    def uId = column[String]("uId")

    def * = (pIndex, pipeName, customPipe, uId) <> (Pipeline.tupled, Pipeline.unapply)
  }
    val Pipelines = TableQuery[PipelineTable]
  
    def insert(pipelineData:Pipeline)=db.run(DBIO.seq( Pipelines += pipelineData ))

    def delete(pIndex: Int)= db.run(Pipelines.filter{_.pIndex === pIndex}.result)
    
    def retrieve(pipelineData: Pipeline):Pipeline= {
      val dbRetrieve = db.run(compiledCheck(pipelineData.pIndex).result)
      val result =Await.result(dbRetrieve, Duration.Inf)
      if(!result.isEmpty)
        return result(0)
      else
        return null
    }
    def update(pipelineData: Pipeline)={

    }
    
    def check(pIndex:Rep[Int]) = Pipelines.filter{pipeline => pipeline.pIndex === pIndex}
    def compiledCheck = Compiled(check _) 
}