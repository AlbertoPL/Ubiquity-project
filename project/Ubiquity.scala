import sbt._

object Ubiquity extends Build {
  lazy val common: Project = Project("Common", file("Common"))
  lazy val client: Project = Project("Client", file("Client")) dependsOn(common)
  lazy val server: Project = Project("Server", file("Server")) dependsOn(common)
}
