package nl.vroste.playjsonoptics
import cats.{Applicative, Traverse}
import monocle.{PTraversal, Prism, Traversal}
import play.api.libs.json._

/**
  * Monocle Optics for [[JsValue]]
  */
object Optics {
  implicit val jsValue: Prism[JsValue, JsValue] = Prism.id
  implicit val jsString: Prism[JsValue, JsString] = prismFromFormat[JsString]
  implicit val jsObject: Prism[JsValue, JsObject] = prismFromFormat[JsObject]

  /**
    * Prism from a JsValue as T using an implicit Format[T] to decode the JsValue
    */
  implicit def prismFromFormat[T](implicit f: Format[T]): Prism[JsValue, T] =
    Prism[JsValue, T](f.reads(_).asOpt)(f.writes)

  val jsArray: Traversal[JsArray, JsValue] = new PTraversal[JsArray, JsArray, JsValue, JsValue] {
    override def modifyF[F[_]](f: JsValue => F[JsValue])(s: JsArray)(implicit F: Applicative[F]): F[JsArray] = s match {
      case JsArray(values) =>
        val fList = Traverse[List].traverse(values.toList)(f)
        F.map(fList)(JsArray(_))
      case _ => F.pure(s)
    }
  }

  def jsArrayOfT[T](implicit prism: Prism[JsValue, T]): Traversal[JsArray, T] =
    jsArray composePrism prism


}

