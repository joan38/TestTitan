name := "TestTitan"

version := "0.1"

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "3.2.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.michaelpollmeier" %% "gremlin-scala" % "2.5.0",
  "com.thinkaurelius.titan" % "titan-berkeleyje" % "0.4.4",
  "com.thinkaurelius.titan" % "titan-es" % "0.4.4",
  "com.thinkaurelius.titan" % "titan-lucene" % "0.4.4"
)

addCommandAlias("load", "run -x resources -d target/database -i titan -q javaPipes1")
