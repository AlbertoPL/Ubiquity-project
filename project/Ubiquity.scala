import sbt._
import com.typesafe.sbtscalariform.ScalariformPlugin

object Ubiquity extends Build {
  
  lazy val root: Project = Project("root", file(".")) aggregate(common, client, server)
  
  lazy val common: Project = Project(id       = "Common", 
				     base     = file("Common"), 
				     settings = Defaults.defaultSettings ++ ScalariformPlugin.settings)
  
  lazy val client: Project = Project(id       = "Client", 
				     base     = file("Client"), 
				     settings = Defaults.defaultSettings ++ ScalariformPlugin.settings) dependsOn(common)
  
  lazy val server: Project = Project(id       = "Server", 
				     base     = file("Server"), 
				     settings = Defaults.defaultSettings ++ ScalariformPlugin.settings) dependsOn(common)
}
