package models

import play.api.db._
import play.api.Play.current
import play.api.libs.json._

import anorm._
import anorm.SqlParser._

case class User(email: String, name: String, password: String)

object User {
  
  // -- Parsers
  
  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.email") ~
    get[String]("user.name") ~
    get[String]("user.password") map {
      case email~name~password => User(email, name, password)
    }
  }
  
  // -- Queries
  
  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    if(email == null || email.length == 0) return None
    else return Some(User(email, "Bob", ""))
    // DB.withConnection { implicit connection =>
    //   SQL("select * from user where email = {email}").on(
    //     'email -> email
    //   ).as(User.simple.singleOpt)
    // }
  }
  
  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user").as(User.simple *)
    }
  }
  
  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    if(email.length == 0) return None
    else return Some(User(email, "Bob", ""))
    // DB.withConnection { implicit connection =>
    //   SQL(
    //     """
    //      select * from user where 
    //      email = {email} and password = {password}
    //     """
    //   ).on(
    //     'email -> email,
    //     'password -> password
    //   ).as(User.simple.singleOpt)
    // }
  }
   
  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into user values (
            {email}, {name}, {password}
          )
        """
      ).on(
        'email -> user.email,
        'name -> user.name,
        'password -> user.password
      ).executeUpdate()
      
      user
      
    }
  }

  implicit object UserFormat extends Format[User] { 
    def reads(json: JsValue): User = null
    def writes(u: User): JsValue = JsObject(List(
      "email" -> JsString(u.email),
      "name" -> JsString(u.name)
    ))
  }
  
}