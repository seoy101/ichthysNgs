package controllers

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
import play.api.libs.json._
import play.api.Logger


class DatabaseController @Inject()(userModel: UserModel ,cache:CacheApi) extends Controller {
 
  private val userForm : Form[User]=Form(mapping(
            "uIndex" -> number,
            "uId"  -> nonEmptyText,
            "password" -> nonEmptyText,
            "email" -> nonEmptyText
            )(User.apply)(User.unapply))
            
  private val imageForm : Form[Image]=Form(mapping(
            "pIndex" -> number,
            "imgName"  -> nonEmptyText,
            "jobName" -> nonEmptyText,
            "jobType" -> nonEmptyText,
            "parentInfo" -> nonEmptyText,
            "status" -> nonEmptyText,
            "date" -> date("yyyy-MM-dd-mm"),
            "uId" -> nonEmptyText
            )(Image.apply)(Image.unapply))
            
   private val pipelineForm : Form[Pipeline]=Form(mapping(
            "pIndex" -> number,
            "pipeName"  -> nonEmptyText,
            "customPipe" -> nonEmptyText,
            "uId" -> nonEmptyText
            )(Pipeline.apply)(Pipeline.unapply))
            
            
  def loginPage = Action{ implicit request =>
    Logger.debug(request.session.get("uId").toString)
    Ok(views.html.login())
  }
  def joinPage = Action { implicit request =>
    Ok(views.html.join())
  }
  
  def addUser = Action { implicit request =>
    val user = userForm.bindFromRequest()
    user.fold(hasErrors={error=>
         Logger.debug(error.toString())
         Ok("nonono")},
        success={newUser => 
          val useradd=Seq("sftp-useradd.sh",newUser.uId,newUser.password)
          Process(useradd).run
          userModel.insert(newUser) 
          Redirect(routes.DatabaseController.loginPage())
          }
      )
  }
  
  def loginUser = Action { implicit request =>
    val user= userForm.bindFromRequest()
       user.fold(hasErrors=>Ok("nonono"),
        success={newUser => 
          val userInfo= userModel.retrieve(newUser)
          if(userInfo==null)
            Redirect(routes.DatabaseController.loginPage())
          else
            Redirect(routes.LaunchController.analysis()).withSession("uId"->userInfo.uId , "password"->userInfo.password, "email"->userInfo.email)  
         }
      )
  }
  
  def delUser = Action { implicit request =>
    val user = userForm.bindFromRequest()
          user.fold(hasErrors=>Ok("nonono"),
        success={newUser => 
          val userInfo= userModel.delete(newUser.uId)     
          Redirect(routes.DatabaseController.loginPage())
          }
      )
  }
  def logoutUser = Action { implicit request => Redirect(routes.DatabaseController.loginPage()).withNewSession}
}



