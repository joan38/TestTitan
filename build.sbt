name := "TestTitan"

version := "1.0"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.thinkaurelius.titan" % "titan-berkeleyje" % "0.4.4",
  "com.thinkaurelius.titan" % "titan-es" % "0.4.4"
)