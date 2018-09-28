import Dependencies._
import Settings._

lazy val `play-json-optics` = (project in file(".")).
  settings(Settings.settings: _*).
  configs(Test)

