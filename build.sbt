import sbt._
import Keys._
import sbt.Extracted
import sbtrelease.ReleasePlugin._
import sbtrelease._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import Utilities._
import com.typesafe.sbt.pgp.PgpKeys.publishSigned

sbtPlugin := true

organization := "info.schleichardt"

name := "sbt-sonar"

//version is in version.sbt and generated by sbt-release

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-Xlint"
)

libraryDependencies += "org.codehaus.sonar.runner" % "sonar-runner-dist" % "2.3"

scalariformSettings

CrossBuilding.scriptedSettings

crossBuildingSettings

CrossBuilding.crossSbtVersions := Seq("0.11.3", "0.12", "0.13")

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
    else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

pomExtra := (
    <url>https://github.com/schleichardt/sbt-sonar</url>
    <licenses>
        <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0</url>
            <distribution>repo</distribution>
        </license>
    </licenses>
    <scm>
        <url>git@github.com:schleichardt/sbt-sonar.git</url>
        <connection>scm:git:git@github.com:schleichardt/sbt-sonar.git</connection>
    </scm>
    <developers>
        <developer>
            <id>schleichardt</id>
            <name>Michael Schleichardt</name>
            <url>http://michael.schleichardt.info</url>
        </developer>
    </developers>
)

releaseSettings

sbtrelease.ReleasePlugin.ReleaseKeys.releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts.copy(action = { st: State =>
        import scala.sys.process._
        if("./sbt-cross.sh publish-signed".! != 0)
          throw new java.io.IOException("could not publish artifacts.")
        st
    }),
    setNextVersion,
    commitNextVersion,
    pushChanges
)