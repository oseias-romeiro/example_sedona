import Dependencies._

ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "Sedona",
    libraryDependencies += scalaTest % Test,
    
    // sedona
    libraryDependencies += "org.apache.sedona" % "sedona-python-adapter-3.0_2.13" % "1.3.1-incubating",
    libraryDependencies += "org.apache.sedona" % "sedona-viz-3.0_2.13" % "1.3.1-incubating",
    libraryDependencies += "org.datasyslab" % "geotools-wrapper" % "1.3.0-27.2",
    // spark
    libraryDependencies += "org.apache.spark" % "spark-core_2.13" % "3.3.1",
    libraryDependencies += "org.apache.spark" % "spark-sql_2.13" % "3.3.1",
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
