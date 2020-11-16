package nl.vroste.playjsonoptics

import org.scalatest.{Inside, OptionValues}
import play.api.libs.json._
import Optics._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

case class DummyData(field1: String)
object DummyData {
  implicit val format: Format[DummyData] = Json.format
}

class OpticsSpec extends AnyFlatSpec with Matchers with Inside with OptionValues {
  "The JsObject prism " must "read a JsObject for the JSON of an object" in {
    val json = Json.obj("field1" -> JsString("value1"))

    jsObject.getOption(json).value mustBe a[JsObject]
  }

  it must "read nothing for the JSON of something other than an object" in {
    val json = Json.arr(JsString("value1"))

    jsObject.getOption(json) mustBe empty
  }

  it must "replace the JSON of an object" in {
    val json = Json.obj("field1" -> JsString("value1"))
    val replacement = Json.obj("field2" -> JsString("value1"))

    jsObject.set(replacement)(json) mustBe replacement
  }

  it must "not replace the JSON of something other than an object" in {
    val json = Json.arr(JsString("value1"))
    val replacement = Json.obj("field2" -> JsString("value1"))

    jsObject.set(replacement)(json) mustBe json
  }

  val dummyDataPrism = prismFromFormat[DummyData]

  "A Prism based on a Format for some type" must "read a value of that type from the JSON of that type" in {
    val json = Json.obj("field1" -> JsString("value1"))
    dummyDataPrism.getOption(json).value mustBe DummyData("value1")
  }

  it must "read nothing for the JSON of something other than the JSON of that type" in {
    val json = Json.obj("fieldDoe" -> JsString("value2"))

    dummyDataPrism.getOption(json) mustBe empty
  }

  it must "replace the JSON given an instance of that type" in {
    val json = Json.obj("field1" -> JsString("value1"))
    val newValue = DummyData("value2")
    val expectedJson = Json.obj("field1" -> JsString("value2"))

    dummyDataPrism.set(newValue)(json) mustBe expectedJson
  }

  "The Traversal for a JsArray" must "read all values in the array given the JSON of an array" in {
    val json = Json.arr("value1", "value2")

    jsArray.getAll(json) must contain theSameElementsAs Seq(JsString("value1"), JsString("value2"))
  }

  it must "modify all values in the array given the JSON of an array" in {
    val json = Json.arr("value1", "value2")

    def modification(jsValue: JsValue): JsValue = jsValue match{
      case JsString(v) => JsString(v + "X")
      case x => x
    }

    jsArray.modify(modification)(json) mustBe Json.arr("value1X", "value2X")
  }

  "The Traversal for a JsArray of some type" must "read all values as instances of that type" in {
    val json = Json.arr(Json.obj("field1" -> "value1"), Json.obj("field1" -> "value2"))

    jsArrayOfT[DummyData].getAll(json) must contain theSameElementsAs Seq(DummyData("value1"), DummyData("value2"))
  }

  it must "modify all values as instances of that type" in {
    val json = Json.arr(Json.obj("field1" -> "value1"), Json.obj("field1" -> "value2"))
    val expectedJson = Json.arr(Json.obj("field1" -> "value1X"), Json.obj("field1" -> "value2X"))

    def modification(v: DummyData): DummyData = v.copy(v.field1 + "X")

    jsArrayOfT[DummyData].modify(modification)(json) mustBe expectedJson
  }
}
