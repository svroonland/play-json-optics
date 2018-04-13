package com.vroste.playjsonoptics.playjsonoptics

import play.api.libs.json.{JsPath, JsValue}
import Optics._

class Helpers {
  implicit class JsPathExtensions(path: JsPath) {
    def moveTo(newPath: JsPath) = {
      val atOldPath = JsLens.optional[JsValue](path)
      val atNewPath = JsLens.optional[JsValue](newPath)

      (json: JsValue) =>
        atOldPath
          .getOption(json)
          .flatten
          .fold(identity[JsValue] _) { value =>
            atNewPath.set(Some(value)) andThen atOldPath.set(None)
          }(json)
    }
  }
}
