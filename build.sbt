name := "SparkApp"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % "1.6.2" % "provided" excludeAll(
    ExclusionRule(organization = "com.fasterxml.jackson")
  ),
  "org.apache.spark" % "spark-sql_2.11" % "1.6.2" % "provided" excludeAll(
    ExclusionRule(organization = "com.fasterxml.jackson")
  ),
  "org.apache.hadoop" % "hadoop-common" % "2.6.0" % "provided" excludeAll(
    ExclusionRule(organization = "javax.servlet")
  ),
  "org.apache.hadoop" % "hadoop-client" % "2.4.0" % "provided" excludeAll(
    ExclusionRule(organization = "javax.servlet")
  ),
  "org.bitbucket.eunjeon" % "seunjeon_2.11" % "1.1.0" excludeAll(
    ExclusionRule(organization = "org.slf4j")
  ),
  "com.typesafe.play" % "play-json_2.11" % "2.5.4" excludeAll(
    ExclusionRule(organization = "com.fasterxml.jackson.core")
  ),
  "org.scala-lang" % "scala-compiler" % "2.11.8"
).map(_.exclude("commons-logging", "commons-logging")
	   .exclude("org.slf4j", "org.slf4j"))

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"
