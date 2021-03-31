
val AkkaVersion = "2.6.13"
val JacksonVersion = "2.10.5.1"



lazy val root =
  Project(id = "root", base = file("."))
    .settings(
      mainClass in (Compile, run) := Some("sample.Main"),
      assemblyJarName in assembly := "produce-consume-kafka-test.jar",
      test in assembly := {},
      assemblyMergeStrategy in assembly := {
      	case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      	case x => MergeStrategy.first
      }, 
	  libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
	  "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.7",
	  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
	  "com.twitter"           %% "bijection-avro"           % "0.9.7",
	  "org.apache.avro" % "avro" % "1.10.2",
	  "com.sksamuel.avro4s" %% "avro4s-core" % "4.0.4",
	  "com.sksamuel.avro4s" %% "avro4s-kafka" % "4.0.4", 
	  "org.scalatest" %% "scalatest" % "3.2.5" % "test",
	  "ch.qos.logback" %  "logback-classic" % "1.2.3" 
	)
)

sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue
watchSources ++= ((avroSourceDirectories in Compile).value ** "*.avrc").get