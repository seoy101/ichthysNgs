package controllers

import play.api.mvc._
import play.api._
import play.api.Logger
import play.api.i18n._
import javax.inject.Inject
import models._
import play.api.data._
import play.api.data.Forms._
import scala.sys.process._
import java.util.Date
import java.io._
import java.text.SimpleDateFormat


import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import java.io.{FileWriter, FileOutputStream, File}

import controllers._
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import java.io.File

import java.nio.file.attribute.PosixFilePermission._
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Files, Path}
import java.nio.file.Paths
import java.util
import javax.inject._

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.actor._
import akka.stream.Materializer



import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart

import play.api.libs.streams.Accumulator

import play.api.mvc.BodyParsers.parse

import play.api.mvc.BodyParsers.parse._
import play.core.parsers.Multipart.FileInfo
import scala.concurrent.Future

import java.io.ByteArrayOutputStream

import play.api.libs.iteratee.Iteratee

import play.api.mvc.BodyParsers.parse._

import play.api.mvc.{BodyParser, MultipartFormData}

import scala.concurrent.ExecutionContext.Implicits.global

import play.core.parsers.Multipart

import play.api.mvc.BodyParsers.parse.multipartFormData

import play.api.libs.streams._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.util.ByteString
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import akka.stream.Materializer
import play.api.mvc.Result

import java.nio.file.attribute.BasicFileAttributes
import akka.stream.scaladsl.{FileIO, Sink}
import akka.stream.scaladsl.FileIO
import java.nio.file.StandardOpenOption

import java.nio.file.StandardOpenOption
import akka.stream.SinkShape
import akka.NotUsed
import reflect.io._
import play.api.libs.json._

class LaunchController @Inject() (implicit system: ActorSystem, materializer: Materializer, val messagesApi:MessagesApi) extends Controller with I18nSupport  {
    
  private val BwaForm : Form[Bwa]=Form(mapping(                        
                                                "exe_name" -> nonEmptyText,
                                                "option1" -> nonEmptyText)(Bwa.apply)(Bwa.unapply)
                                                      ) 
  
  def index= Action{
    implicit request =>
      Ok(views.html.bwa())
  }
  
  def analysis = Action{
     implicit request =>        
         Ok(views.html.config())
  }
  
  def launch =Action{
    implicit request =>
      
        request.session.get("uId")match {
          case Some(value) => {
            val user = value
            val conf= Json.parse(request.body.asFormUrlEncoded.get.get("conf").get(0));
            val pipeline= Json.parse(request.body.asFormUrlEncoded.get.get("pipeline").get(0));      
            val job_name = (conf \ "job_name").as[String]
            val cpu = (conf \ "cpu").as[String]
            val mem = (conf \ "mem").as[String]
            val exes = (pipeline \\ "exe").map(_.as[String])      
            val fileName=(pipeline \ "fileName").as[String]
            
            val userPath="/nfsdir/"+user+"/"
            val filePath=userPath+job_name
            
            Process("mkdir -p "+filePath).run
            Process("chmod 777 "+filePath).run   
            Process("touch "+filePath+"/Dockerfile").run
            Process("touch "+filePath+"/innerSh.sh").run
            writingDockerfile(filePath)
            writingInnerSh(userPath,filePath,fileName,exes);
//      
      
  
            Logger.debug(job_name+"/"+cpu+"/"+mem+"/"+exes+"/"+filePath);    
            Logger.debug(pipeline.toString());
            }
          }
     
     
      
//      Logger.debug(request.body.toString())
//      val job_name = 
//      val filePath = "/nfsdir/"+job_name
      Ok("asdf");
//      val bwa=BwaForm.bindFromRequest()
//       bwa.fold(
//           hasErrors=> Ok("input error")
//           , success= { newBwa=>           
//                          val now= (new SimpleDateFormat("yyyy-MM-dd-HH-mm")).format(new Date).toString()
//                          val exe_name=bwa.data("exe_name")
//                          val job_name=exe_name+"-"+now
//                          val job_path="/nfsdir/"+job_name
//                          val fileName= "MT.fa"
//                          Process("mkdir "+job_path).run
//                          Process("chmod 777 "+job_path).run    
//                          Process("touch "+job_path+"/innerSh.sh").run
//                          Process("touch "+job_path+"/Dockerfile").run
//                          
//                          val cmd=Bwa.getCmd(newBwa)
//                          
//                          writingInnerSh(job_path,exe_name,fileName,job_name,cmd+fileName )
//                          writingDockerfile(job_path)
//                          
//                    
//                          val launch=Seq("launch.sh",job_path,job_name)
//                          Process(launch).run
//                          
//                          Ok(cmd)
//             }
//           )
  }  
  
  def getHtml(data:String) = Action{
    implicit request =>
      val text= "views.html."+data       
      Logger.debug(text)
      if(data=="test")
         Ok(views.html.test())
      else if(data=="test2")
          Ok(views.html.test2())
      else if(data=="test3")
          Ok(views.html.test3())
      else if(data=="pipeline")
        Ok(views.html.pipeline())
        else if (data=="message")
          Ok(views.html.message())
      else
        Ok("nononononosxxxx")
  }
  
  
  //////////////////
def fileupload =Action{
  implicit request =>
    Ok(views.html.fileupload())
}
  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  def handleFilePartAsFile: FilePartHandler[File] = {
        case FileInfo(partName, filename, contentType) =>
          val filepath = Paths.get("/nfsdir/test")   
         val filesink :Sink[ByteString, Future[IOResult]] =FileIO.toPath(filepath,Set(StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE))        
         val accumulator = Accumulator(filesink)        
         accumulator.map { case IOResult(count, status) =>                    
          FilePart(partName, filename, contentType, filepath.toFile())
        }(play.api.libs.concurrent.Execution.defaultContext)
        } 

def uploadCustom = Action(parse.multipartFormData(handleFilePartAsFile,1000000000000L)) { request =>
  val fileOption = request.body.file("name").map {
    case FilePart(key, filename, contentType, file) =>
      file.toPath
  }

  Ok(s"File uploaded: $fileOption")
}
//////////////////////
  def sftpresult(data:String) = Action{
    implicit request =>
      val user = request.session.get("uId").toString();
      val result=Seq("mv","-f","/nfsdir/"+data,"/home/"+user)
      Process(result).run 
      Ok("ok")
       
      
  }
  
   private def writingDockerfile(filePath :String) = {
    val bw = new BufferedWriter(new FileWriter(filePath+"/Dockerfile"))
     bw.write("FROM yuhadam/baseimage")
     bw.newLine()
     bw.write("RUN apt-get update")
     bw.newLine()
     bw.write("RUN apt-get install -y curl")
     bw.newLine()
     bw.write("RUN apt-get install -y nfs-common")
     bw.newLine()
     bw.write("WORKDIR "+filePath+"/")
     bw.newLine()
     bw.write("CMD ./innerSh.sh")
     bw.close()
  }              
   private def writingInnerSh(userPath:String,filePath:String, fileName:String, exes:Seq[String])={
     val bw = new BufferedWriter(new FileWriter(filePath+"/innerSh.sh"))
     bw.write("#! /bin/sh")
     bw.newLine()
     bw.write("set -e")
     bw.newLine()
     for(exe <- exes){
       bw.write("ln /exe/"+exe+" "+filePath)
       bw.newLine()
     }
     bw.write("ln "+userPath+" "+fileName+" "+filePath)
     bw.newLine()
     bw.close();
//     bw.write(cmd)
//     bw.newLine()
//     bw.write("rm "+exe_name)
//     bw.newLine()
//     bw.write("rm "+fileName)
//     bw.newLine()
//     bw.write("rm Dockerfile")
//     bw.newLine()
//     bw.write("rm docker.json")
//     bw.newLine()
//     bw.write("rm innerSh.sh")
//     bw.newLine()
//     bw.write("curl 211.249.63.201:9000/result/"+job_name)
//     bw.newLine()
//     bw.close()
//     Process("sudo chmod 777 "+filePath+"/innerSh.sh").run
                       
   }
  
 
}