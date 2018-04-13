import Dependencies._
import Settings._

lazy val playjsonoptics = (project in file("playjsonoptics")).
  settings(Settings.settings: _*).
  settings(Settings.playjsonopticsSettings: _*).
  configs(Test)

