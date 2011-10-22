import sbt._

object Ubiquity extends Build {
  lazy val root: Project = Project("root", file(".")) aggregate(common, client, server, web)
  lazy val common: Project = Project("Common", file("Common")) 
  lazy val client: Project = Project("Client", file("Client")) dependsOn(common)
  lazy val server: Project = Project("Server", file("Server")) dependsOn(common)
  lazy val web: Project = Project("WebUbiquity", file("WebUbiquity")) dependsOn(common, server)
}
