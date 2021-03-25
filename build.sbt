
val AkkaVersion = "2.6.13"
val JacksonVersion = "2.10.5.1"


lazy val root =
  Project(id = "root", base = file("."))
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
	  "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.7",
	  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
	  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
	  "org.scalatest" %% "scalatest" % "3.2.5" % "test",

	)
)