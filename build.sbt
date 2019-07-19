organization in ThisBuild := "com.dautechnologies"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `assetmanagementservice` = (project in file("."))
  .aggregate(`assetmanagementservice-api`, `assetmanagementservice-impl`, `assetmanagementservice-stream-api`, `assetmanagementservice-stream-impl`)

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

lazy val `assetmanagementservice-stream-api` = (project in file("assetmanagementservice-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `assetmanagementservice-stream-impl` = (project in file("assetmanagementservice-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`assetmanagementservice-stream-api`, `assetmanagementservice-api`)
