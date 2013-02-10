//
// Socko Web Server build file
//

import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
import sbt.Project.Initialize
import sbtassembly.Plugin._
import AssemblyKeys._

//
// Build setup
//
object SockoBuild extends Build {

  //
  // Settings
  //
  lazy val defaultSettings = Defaults.defaultSettings ++ Seq(
    // Info
    organization := "org.mashupbots.socko",
    version      := "0.2.4",
    scalaVersion := "2.10.0",
    organizationHomepage := Some(url("http://www.sockoweb.org")),

    // Repositories
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
    
    // Compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-optimize", "-feature"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    
    // sbtEclipse - see examples https://github.com/typesafehub/sbteclipse/blob/master/sbteclipse-plugin/src/sbt-test/sbteclipse/02-contents/project/Build.scala
    EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.Unmanaged, EclipseCreateSrc.Source, EclipseCreateSrc.Resource),
    EclipseKeys.withSource := true    
  )
  
  lazy val doNotPublishSettings = Seq(publish := {}, publishLocal := {})
   
  //
  // Packaging to SonaType using SBT
  //
  // https://github.com/sbt/sbt.github.com/blob/gen-master/src/jekyll/using_sonatype.md
  // http://www.cakesolutions.net/teamblogs/2012/01/28/publishing-sbt-projects-to-nexus/
  // https://docs.sonatype.org/display/Repository/How+To+Generate+PGP+Signatures+With+Maven
  //    
  def sockoPomExtra = {
    <url>http://www.sockoweb.org</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:mashupbots/socko.git</url>
      <connection>scm:git:git@github.com:mashupbots/socko.git</connection>
    </scm>
    <developers>
      <developer>
        <id>veebs</id>
        <name>Vibul Imtarnasan</name>
        <url>https://github.com/veebs</url>
      </developer>
      <developer>
        <id>lightningdb</id>
        <name>David Bolton</name>
        <url>https://github.com/lightningdb</url>
      </developer>
    </developers>
  }

  def sockoPublishTo: Initialize[Option[Resolver]] = {
    (version) { version: String =>
      val nexus = "https://oss.sonatype.org/"
      if (version.trim.endsWith("SNAPSHOT")) {
        Some("snapshots" at nexus + "content/repositories/snapshots/")
      } else {
        Some("releases" at nexus + "service/local/staging/deploy/maven2/")
      }
    }
  }
    
  //
  // Projects
  //
  lazy val root = Project(id = "socko",
                          base = file("."),
                          settings = defaultSettings) aggregate(webserver, examples)

  lazy val webserver = Project(id = "socko-webserver",
                         base = file("socko-webserver"),
                         settings = defaultSettings ++ Seq(
                           libraryDependencies ++= Dependencies.webserver,
                           publishTo <<= sockoPublishTo,
                           publishMavenStyle := true,
                           publishArtifact in Test := false,
                           pomIncludeRepository := { x => false },
                           pomExtra := sockoPomExtra
                         ))
                         
  lazy val examples = Project(id = "socko-examples",
                         base = file("socko-examples"),
                         dependencies = Seq(webserver),
                         settings = defaultSettings ++ doNotPublishSettings ++ Seq(
                           libraryDependencies ++= Dependencies.examples
                         ))  
}

//
// Dependencies
//
object Dependencies {
  import Dependency._

  val webserver = Seq(
    Dependency.akkaActor, Dependency.akkaSlf4j, Dependency.akkaTestKit,
    Dependency.netty, Dependency.concurrentmap, Dependency.nextProtoNeg,
    Dependency.logback, Dependency.scalatest
  )
  
  val examples = Seq(
    Dependency.logback
  )  
}

object Dependency {
  object V {
    val Akka        = "2.1.0"
  }

  val akkaActor     = "com.typesafe.akka"                       %% "akka-actor"                  % V.Akka
  val akkaSlf4j     = "com.typesafe.akka"                       %% "akka-slf4j"                  % V.Akka
  val akkaTestKit   = "com.typesafe.akka"                       %% "akka-testkit"                % V.Akka % "test"
  val netty         = "io.netty"                                % "netty"                        % "3.6.2.Final"
  val concurrentmap = "com.googlecode.concurrentlinkedhashmap"  % "concurrentlinkedhashmap-lru"  % "1.3.2"
  val nextProtoNeg  = "org.eclipse.jetty.npn"                   % "npn-api"                      % "1.1.0.v20120525"
  val logback       = "ch.qos.logback"                          % "logback-classic"              % "1.0.9" % "runtime"
  val scalatest     = "org.scalatest"                           % "scalatest_2.10"               % "2.0.M5b" % "test"
}




