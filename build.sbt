import Dependencies._
import Settings._

lazy val root = (project in file("playjsonoptics")).
  settings(Settings.settings: _*).
  configs(Test)

