package com.vroste.playjsonoptics

import play.api.libs.json.{Format, JsPath, JsValue}

object Helpers {
  implicit class JsPathExtensions(path: JsPath) {
    def moveTo(newPath: JsPath): JsValue => JsValue = {
      val atOldPath = JsLens.optional[JsValue](path)
      val atNewPath = JsLens.optional[JsValue](newPath)

      (json: JsValue) =>
        atOldPath
          .getOption(json)
          .flatten
          .fold(identity[JsValue] _) { value =>
            atNewPath.set(Some(value))
          }(json)
    }

    /**
      * Transformation that moves the value at this path to a new path
      *
      * The current path is pruned
      */
    def moveTo(newPath: JsPath): JsValue => JsValue = {
      copyTo(newPath) andThen JsLens.optional[JsValue](path).set(None)
    }

    /**
      * Transformation that sets a default value at the given path if the path is missing
      */
    def setDefault[T : Format](defaultValue: T): JsValue => JsValue =
      JsLens.optional[T](path) modify (_ orElse Some(defaultValue))
  }
}
