name := "Ubiquity"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at 
	     "http://repo.typesafe.com/typesafe/releases" 

seq(com.typesafe.sbtscalariform.ScalariformPlugin.settings: _*)

seq(webSettings :_*)

libraryDependencies ++= {
  val liftVersion = "2.4-M4"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default")
}

libraryDependencies ++= Seq(
		    "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
		    "se.scalablesolutions.akka" % "akka-actor" % "1.2", 
		    "se.scalablesolutions.akka" % "akka-remote" % "1.2"
)
