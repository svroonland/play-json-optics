import Dependencies._
import Settings._

lazy val root = (project in file(".")).
  settings(Settings.settings: _*).
  configs(Test)

