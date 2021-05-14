name := "SquerylTest"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies  ++=  Seq(
  "org.squeryl" %% "squeryl" % "0.9.16",
  "com.h2database" % "h2" % "1.3.157"
)
