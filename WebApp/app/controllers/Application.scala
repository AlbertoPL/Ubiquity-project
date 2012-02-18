package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def devices = Action {
    Ok(views.html.devices())
  }

  def projects = Action {
  	Ok(views.html.projects())
  }
}