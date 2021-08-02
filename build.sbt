name := "big-data-crawler"
ThisBuild / organization := "org.ua.wozzya"
ThisBuild / scalaVersion := "2.12.2"


lazy val root = (project in file("."))
  .settings(
    assembly / assemblyJarName := "root.jar"
  )
  .aggregate(crawler, sparkProcessor)


lazy val crawler = (project in file("crawler"))
  .settings(
    name := "crawler",
    libraryDependencies ++= Seq(
      dependencies.googleOauthClientJetty,
      dependencies.googleApiServiceYouTube,
      dependencies.googleHttpClientJakson2,
      dependencies.apacheCommonsLang3,
      dependencies.commonsCli,
      dependencies.orgJson,
      dependencies.lambdaEvents,
      dependencies.lambdaCore,
      dependencies.s3
    )
  ).settings(
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs@_*) => MergeStrategy.discard
    case "module-info.class" => MergeStrategy.discard
    case _ => MergeStrategy.first
  },
  assembly / assemblyCacheOutput := false,
  assembly / assemblyOutputPath := file("./jars/crawler.jar"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

lazy val sparkProcessor = (project in file("spark-processor"))
  .settings(
    name := "spark-processor",
    libraryDependencies ++= Seq(
      dependencies.sparkCore,
      dependencies.sparkSql,
    ),
    assembly / assemblyOutputPath := file("./jars/spark.jar")
  )

lazy val dependencies = new {
  private val apiServicesYouTubeV = "v3-rev222-1.25.0"
  private val oauthClientV = "1.31.5"
  private val httpClientV = "1.38.0"
  private val commonsLangV = "3.12.0"
  private val commonsCliV = "1.4"
  private val jsonV = "20210307"
  private val sparkCoreV = "3.1.1"
  private val sparkSqlV = "3.1.1"
  private val lambdaCoreVersion = "1.2.0"
  private val lambdaEventsVersion = "3.6.0"
  private val s3Version = "1.12.26"

  val googleApiServiceYouTube = "com.google.apis" % "google-api-services-youtube" % apiServicesYouTubeV
  val googleOauthClientJetty = "com.google.oauth-client" % "google-oauth-client" % oauthClientV
  val googleHttpClientJakson2 = "com.google.http-client" % "google-http-client-jackson2" % httpClientV
  val apacheCommonsLang3 = "org.apache.commons" % "commons-lang3" % commonsLangV
  val commonsCli = "commons-cli" % "commons-cli" % commonsCliV
  val orgJson = "org.json" % "json" % jsonV


  val sparkCore = "org.apache.spark" %% "spark-core" % sparkCoreV % "provided"
  val sparkSql = "org.apache.spark" %% "spark-sql" % sparkSqlV % "provided"

  val lambdaCore = "com.amazonaws" % "aws-lambda-java-core" % lambdaCoreVersion
  val lambdaEvents = "com.amazonaws" % "aws-lambda-java-events" % lambdaEventsVersion
  val s3 = "com.amazonaws" % "aws-java-sdk-s3" % s3Version
}
