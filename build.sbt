name := "Ubiquity"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at 
	      "http://repo.typesafe.com/typesafe/releases" 

libraryDependencies ++= Seq(
		    "se.scalablesolutions.akka" % "akka-actor" % "1.2", 
		    "se.scalablesolutions.akka" % "akka-remote" % "1.2"
)
