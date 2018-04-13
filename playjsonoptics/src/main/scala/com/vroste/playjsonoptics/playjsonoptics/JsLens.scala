package com.vroste.playjsonoptics.playjsonoptics

import monocle.{Lens, Optional, Prism, Traversal}
import play.api.libs.json.{JsArray, JsObject, JsPath, JsValue}
import Optics._
import monocle.std.option.some
import cats.instances.option._

/**
  * Create Optics from a [[JsPath]]
  *
  * Using the path syntax is easier then manually composing optics
  */
object JsLens {

  /**
    * Creates an Optional to a value of type T at the given path in a JSON value
    *
    * Can be used to get, set or modify the value at the given path, while
    * working with values of type T instead of JsValue. The prism takes care of the
    * translation between JsValue and T.
    *
    * Example:
    * {{{
    *   JsLens[String](__ \ "value1") modify (_ + "with suffix")
    * }}}
    *
    * @param path
    * @param prism Prism between [[play.api.libs.json.JsValue]] and [[T]]. Typically derived from a Format[T]
    * @tparam T
    * @return
    */
  def apply[T](path: JsPath)(implicit prism: Prism[JsValue, T]): Optional[JsValue, T] =
    (jsObject
      composeLens optionalValueAtPath(path)
      composePrism some
      composePrism prism)

  /**
    * An optional to a JsValue
    */
  def apply(path: JsPath): Optional[JsValue, JsValue] =
    (jsObject
      composeLens optionalValueAtPath(path)
      composePrism some)

  /**
    * Creates an Optional to an optional value of T at the given path in a JSON value
    *
    * Is useful next to [[apply]] to create an optic that can be used to delete the value
    * at the given path altogether (prune the path). This can be done by calling {{{set(None)}}}
    *
    * @param path
    * @param prism
    * @tparam T
    * @return
    */
  def optional[T](path: JsPath)(implicit prism: Prism[JsValue, T]): Optional[JsValue, Option[T]] =
    (jsObject
      composeLens optionalValueAtPath(path)
      composePrism prism.below[Option]) // Maps Prism[A, B] to Prism[Option[A], Option[B]]

  /**
    * Creates an optic for each of the children in a JsArray at the given path
    *
    * Useful to apply modifications to each element. The [[monocle.Traversal]] can be further composed
    * with other JsPath optics to apply modifications to elements deeper in the array
    *
    * @param path
    * @param prism
    * @tparam T
    * @return
    */
  def each[T](path: JsPath)(implicit prism: Prism[JsValue, T]): Traversal[JsValue, T] =
    (JsLens[JsArray](path)
      composeTraversal jsArray
      composePrism prism)

  /**
    * Lens to optional value at the given path
    *
    * When a non-empty JsValue is set, it is deep merged with the existing JSON
    * at that path. An empty value will result in the path being pruned
    */
  private def optionalValueAtPath(path: JsPath) =
    Lens[JsObject, Option[JsValue]](path.asSingleJsResult(_).asOpt) {
      valueOpt =>
        root =>
          valueOpt.fold(path.json.prune.reads(root).getOrElse(root)) { value =>
            root ++ JsPath.createObj((path, value))
          }
    }
}

