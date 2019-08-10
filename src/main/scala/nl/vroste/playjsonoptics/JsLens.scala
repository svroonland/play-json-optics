package nl.vroste.playjsonoptics

import cats.instances.option._
import Optics._
import monocle.function.At
import monocle.std.option.some
import monocle.{Lens, Optional, Traversal}
import play.api.libs.json._

/**
  * Create Optics from a [[JsPath]]
  *
  * Using the path syntax and [[JsLens]] is easier then manually composing lenses and prisms for some path.
  * The intermediate optics are derived from [[Format]]s.
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
    *   JsLens[String](__ \ "value1") modify (_ + "additional characters")
    * }}}
    *
    * @param path
    * @tparam T Type of the value to work with, given that a [[Format]] is available.
    *           Can also be a subtype of [[JsValue]].
    * @return
    */
  def apply[T: Format](path: JsPath): Optional[JsValue, T] =
    (optionalValueAtPath(path)
      composePrism some
      composePrism prismFromFormat)

  /**
    * Creates an Optional to an optional value of T at the given path in a JSON value
    *
    * Is useful next to [[apply]] to create an optic that can be used to delete the value
    * at the given path altogether (prune the path). This can be done by calling {{{set(None)}}}
    *
    * @param path
    * @tparam T
    * @return
    */
  def optional[T: Format](path: JsPath): Optional[JsValue, Option[T]] =
    (optionalValueAtPath(path)
      composePrism prismFromFormat.below[Option]) // Maps Prism[A, B] to Prism[Option[A], Option[B]]

  /**
    * Creates an optic for each of the children in a JsArray at the given path
    *
    * Useful to apply modifications to each element. The [[monocle.Traversal]] can be further composed
    * with other JsPath optics to apply modifications to elements deeper in the array
    *
    * @param path
    * @tparam T
    * @return
    */
  def each[T: Format](path: JsPath): Traversal[JsValue, T] =
    (JsLens[JsArray](path)
      composeTraversal jsArray
      composePrism prismFromFormat)

  /**
    * Lens to optional value at the given path
    *
    * When a non-empty JsValue is set, it is deep merged with the existing JSON
    * at that path. An empty value will result in the path being pruned
    */
  private def optionalValueAtPath(path: JsPath): Lens[JsValue, Option[JsValue]] =
    Lens[JsValue, Option[JsValue]](path.asSingleJsResult(_).asOpt) {
      newValueOpt =>
        {
          case rootObj@JsObject(_) =>
            newValueOpt match {
              case Some(obj) =>
                createAtPath(path, obj) match {
                  // Deep merge the root object with the new value at the given path
                  case o : JsObject =>
                    rootObj deepMerge o
                  case v @ _ =>
                    // Replace
                    v
                }
              case None =>
                path.json.prune.reads(rootObj).getOrElse(rootObj)
            }
          case root =>
            newValueOpt match {
              case Some(obj) =>
                createAtPath(path, obj)
              case None =>
                path.json.prune.reads(root).getOrElse(root)
            }
        }
    }

  /**
    * Creates a JsObject with a value at some (possibly nested) sub path
    *
    * @param path
    * @param value
    */
  private def createAtPath(path: JsPath, value: JsValue): JsValue =
    if (path == __) {
      value
    } else {
      JsPath.createObj((path, value))
    }

  // Allows remove syntax
  implicit val jsAt: At[JsValue, JsPath, Option[JsValue]] = optionalValueAtPath(_)
}

