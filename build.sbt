organization in ThisBuild := "com.dautechnologies"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val scodecCore = "org.scodec" %% "scodec-core" % "1.11.4"
val scodecBits = "org.scodec" %% "scodec-bits" % "1.1.12"
val scalaCSV = "com.github.tototoshi" %% "scala-csv" % "1.3.6"


lazy val `assetmanagementservice` = (project in file("."))
  .aggregate(`assetmanagementservice-api`, `assetmanagementservice-impl`)

lazy val `assetmanagementservice-api` = (project in file("assetmanagementservice-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `assetmanagementservice-impl` = (project in file("assetmanagementservice-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`assetmanagementservice-api`)

