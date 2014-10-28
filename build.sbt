name := "InterViewer"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.json" % "json" % "20140107",
  "org.apache.commons" % "commons-io" % "1.3.2"
)     

play.Project.playJavaSettings
