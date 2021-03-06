/*
 * Copyright 2019 is-land
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.Keys._
import sbt.io.IO

ThisBuild / scalaVersion := "2.12.9"
ThisBuild / cancelable in Global := true
ThisBuild / shellPrompt := { s => "sbt:" + Project.extract(s).currentProject.id + "> " }
ThisBuild / scalafmtConfig := Option(file("checkstyle/.scalafmt.conf"))
ThisBuild / Test / fork := true
ThisBuild / Test / javaOptions += "-Xmx1G"


lazy val modules: Seq[ProjectReference] = Seq(
  agent, client, common, configurator, kafka,
  metrics, shabondi, streams, `testing-util`
)


lazy val root = project.in(file("."))
        .aggregate(modules: _*)
        .settings(
          commonSettings,
          onLoadMessage :=
                  """
                    |** Welcome to the sbt build definition for OharaStream! **
                  """.stripMargin
        )


lazy val common = oharaProject("common",
  inConfig(Compile)(Seq(
    compile := compile.dependsOn(Def.sequential(
      VersionUtilsGenerator.generate(thisProject, javaSource, sLog),
      headerCreate
    )).value
  )),
  Seq(libraryDependencies ++= Seq(
    libs.commonsNet,
    libs.commonsLang,
    libs.slf4jApi,
    libs.slf4jLog4j,
    libs.commonsIo,
    libs.guava,
    libs.mockito % Test,
    libs.junit % Test
  ))
)

lazy val `testing-util` = oharaProject("testing-util",
  Seq(libraryDependencies ++= Seq(
    libs.sshd excludeAll (libs.sshdExclusionRules: _*),
    libs.kafkaCore,
    libs.kafkaConnectJson,
    libs.kafkaConnectRuntime,
    libs.mysql,
    libs.embeddedsql,
    libs.ftpServer,
    libs.scalaLogging,
    libs.slf4jApi,
    libs.slf4jLog4j,

    /**
      * The Hadoop use jersey 1.x, but the Kafka use jersey 2.x so jar conflict
      *
      * Solve running Kafka Connect mini cluster failed.
      */
    libs.hadoopCommon excludeAll (libs.hadoopExclusionRules: _*),
    libs.hadoopHdfs excludeAll (libs.hadoopExclusionRules: _*),

    libs.scalatest % Test,
    libs.mockito % Test,
    libs.junit % Test
  ))
).dependsOn(
  common % "compile->compile; test->test"
)


lazy val metrics = oharaProject("metrics",
  Seq(libraryDependencies ++= Seq(
    libs.slf4jApi,
    libs.slf4jLog4j,

    libs.junit % Test
  ))
).dependsOn(
  common % "compile->compile; test->test",
  `testing-util` % "compile->test; test->test"
)


lazy val kafka = oharaProject("kafka",
  Seq(libraryDependencies ++= Seq(
    libs.kafkaClient,
    libs.kafkaConnectFile,
    libs.kafkaConnectRuntime,
    libs.slf4jApi,
    libs.slf4jLog4j
  ),
    libraryDependencies ++= Seq(
      libs.hadoopHdfs % Test excludeAll (libs.hadoopExclusionRules: _*),
      libs.mockito % Test,
      libs.junit % Test
    )
  )
).dependsOn(
  common % "compile->compile; test->test",
  `testing-util` % "compile->test; test->test",
  metrics % "compile->compile"
)


lazy val client = oharaProject("client",
  Seq(
    Test / javaOptions += "-Xmx1500M"
  ),
  Seq(libraryDependencies ++= Seq(
    libs.commonsNet,
    libs.commonsLang,
    libs.scalaLogging,
    libs.slf4jApi,
    libs.slf4jLog4j,
    libs.akkaStream,
    libs.akkaHttpSprayJson,
    libs.kafkaClient,
    libs.kafkaConnectFile,
    libs.kafkaConnectRuntime,
    libs.scalatest % Test,
    libs.mockito % Test,
    libs.junit % Test
  ))
).dependsOn(
  common % "compile->compile; test->test",
  `testing-util` % "compile->test; test->test",
  kafka % "compile->compile"
)


lazy val agent = oharaProject("agent",
  Seq(libraryDependencies ++= Seq(
    libs.scalaLogging,
    libs.slf4jApi,
    libs.slf4jLog4j,
    libs.akkaStream,
    libs.akkaHttpSprayJson,
    libs.sshd excludeAll (libs.sshdExclusionRules: _*),
    libs.commonsIo,

    libs.scalatest % Test,
    libs.junit % Test
  ))
).dependsOn(
  common % "compile->compile; test->test",
  kafka % "compile->compile",
  client % "compile->compile",
  metrics % "compile->compile",
  `testing-util` % "compile->test; test->test",
)

lazy val streams = oharaProject("streams",
  Seq(libraryDependencies ++= Seq(
    libs.commonsLang,
    libs.slf4jApi,
    libs.slf4jLog4j,
    libs.kafkaStreams,
    libs.commonsIo,

    libs.mockito % Test,
    libs.junit % Test,
    libs.javassist % Test
  ))
).dependsOn(
  common % "compile->compile; test->test",
  kafka % "compile->compile",
  `testing-util` % "compile->test; test->test",
)


lazy val shabondi = oharaProject("shabondi",
  Seq(libraryDependencies ++= Seq(
    libs.scalaLogging,
    libs.slf4jApi,
    libs.slf4jLog4j,
    libs.akkaStream,
    libs.akkaHttp,

    libs.scalatest % Test,
    libs.akkaHttpTestKit % Test,
    libs.mockito % Test,
    libs.junit % Test
  ))
).dependsOn(
  common % "compile->compile; test->test"
)


lazy val connector = oharaProject("connector",
  Seq(libraryDependencies ++= Seq(
    libs.scalaLogging,
    libs.slf4jApi,
    libs.slf4jLog4j,
    libs.akkaHttpSprayJson,
    libs.kafkaConnectRuntime,

    /**
      * The Hadoop use jersey 1.x, but the Kafka use jersey 2.x so jar conflict
      *
      * Solve running Kafka Connect mini cluster failed.
      */
    libs.hadoopCommon excludeAll (libs.hadoopExclusionRules: _*),
    libs.hadoopHdfs excludeAll (libs.hadoopExclusionRules: _*),

    libs.scalatest % Test,
    libs.mockito % Test,
    libs.junit % Test
  ))
).dependsOn(
  common % "compile->compile; test->test",
  client % "compile->compile",
  kafka % "compile->compile",
  metrics % "compile->compile",
  `testing-util` % "compile->test; test->test",
)


lazy val configurator = oharaProject("configurator",
  Seq(
    Test / javaOptions += "-Xmx1500M"
  ),
  Seq(libraryDependencies ++= Seq(
    /**
      * The Hadoop use jersey 1.x, but the Kafka use jersey 2.x so jar conflict
      *
      * Solve running Kafka Connect mini cluster failed.
      */
    libs.hadoopCommon excludeAll (libs.hadoopExclusionRules: _*),
    libs.hadoopHdfs excludeAll (libs.hadoopExclusionRules: _*),

    libs.kafkaClient,
    libs.kafkaConnectFile,
    libs.kafkaConnectRuntime,
    libs.scalaLogging,
    libs.slf4jApi,
    libs.slf4jLog4j,
    libs.akkaStream,
    libs.akkaHttpSprayJson,
    libs.rocksdb,

    libs.scalatest % Test,
    libs.mockito % Test,
    libs.junit % Test
  ))
).dependsOn(
  agent % "compile->compile",
  client % "compile->compile",
  common % "compile->compile; test->test",
  kafka % "compile->compile",
  streams % "compile->compile",
  metrics % "compile->compile",
  connector % "compile->test",
  `testing-util` % "compile->test; test->test"
)

val checkFormat = taskKey[Unit]("Check all the source code which includes src, test, and build files")

def oharaProject(projectId: String, additionalSettings: sbt.Def.SettingsDefinition*) =
  Project(projectId,
    base = file(s"ohara-$projectId")
  ).settings(
    name := s"ohara-$projectId",
  ).settings(
    commonSettings
  ).settings(
    /*
     * Generate copyright header
     */
    Seq(Compile, Test).flatMap { conf =>
      inConfig(conf)(Seq(
        headerLicense := Some(copyrightLicense(baseDirectory.value / "..")),
        compile := compile.dependsOn(headerCreate, checkFormat).value
      ))
    }
  ).settings(additionalSettings: _*)


lazy val commonSettings =
  Seq(
    /* Run junit with sbt
     * Reference:
     *   https://stackoverflow.com/q/28174243/3155650
     *   https://github.com/sbt/junit-interface
     */
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v", "-a"),
    libraryDependencies ++= Seq(
      libs.scalatest % Test,
      libs.junitInterface % Test exclude("junit", "junit-dep")
    )) ++ Seq(
    /*
     * Checkstyle
     */
    checkFormat := {
      (scalafmtCheck in Compile).value
      (scalafmtCheck in Test).value
      (scalafmtSbtCheck in Compile).value
    }
  )


def copyrightLicense(rootDir: File): HeaderLicense.Custom = {
  val file = new File(rootDir, "checkstyle/apacheV2.header")
  HeaderLicense.Custom(IO.read(file))
}
