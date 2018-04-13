package com.vroste.playjsonoptics

import org.scalatest.{FlatSpec, Inside, MustMatchers, OptionValues}
import play.api.libs.json._

class JsLensSpec extends FlatSpec with MustMatchers with Inside with OptionValues {
  "A JsLens for a single String value at some path" must "read the value at that path given applicable JSON" in {
    val json = Json.obj("field1" -> JsString("value1"))

    val optic = JsLens[String](__ \ "field1")
    optic.getOption(json).value mustBe "value1"
  }

  it must "not read the value at that path when that path is missing" in {
    val json = Json.obj()

    JsLens[String](__ \ "field1").getOption(json) mustBe empty
  }

  it must "modify the value at some path given applicable JSON" in {
    val json = Json.obj("field1" -> JsString("value1"))
    val expectedJson = Json.obj("field1" -> JsString("value1X"))

    val optic = JsLens[String](__ \ "field1")
    optic.modify(_ + "X")(json) mustBe expectedJson
  }

  it must "not modify the value at some path when that path is missing" in {
    val json = Json.obj()
    val expectedJson = Json.obj()

    val optic = JsLens[String](__ \ "field1")
    optic.modify(_ + "X")(json) mustBe expectedJson
  }

  "An optional JsLens for a single String value at some path" must "read the value at that path given applicable JSON" in {
    val json = Json.obj("field1" -> JsString("value1"))

    val optic = JsLens.optional[String](__ \ "field1")
    optic.getOption(json).value mustBe Some("value1")
  }

  it must "create intermediate paths when setting a value at some path in empty JSON" in {
    val json = Json.obj()

    val optic = JsLens.optional[String](__ \ "a" \ "b")

    optic.set(Some("c"))(json) mustBe Json.obj("a" -> Json.obj("b" -> JsString("c")))
  }

  it must "remove the value at that path" in {
    val json = Json.obj("field1" -> JsString("value1"), "field2" -> JsString("value2"))
    val expectedJson = Json.obj("field1" -> JsString("value1"))

    val optic = JsLens.optional[String](__ \ "field2")
    optic.set(None)(json) mustBe expectedJson
  }

  "A Traversal JsLens for for a String at some path" must "read the values at that path given applicable JSON" in {
    val json = Json.obj("field1" -> Json.arr(JsString("value1"), JsString("value2")))

    val optic = JsLens.each[String](__ \ "field1")
    optic.getAll(json) mustBe List("value1", "value2")
  }

  it must "read values at the root path" in {
    val json = Json.arr(JsString("value1"), JsString("value2"))

    val optic = JsLens.each[String](__)
    optic.getAll(json) mustBe List("value1", "value2")
  }

  it must "modify all values given applicable JSON" in {
    val json = Json.obj("field1" -> Json.arr(JsString("value1"), JsString("value2")))
    val expectedJson = Json.obj("field1" -> Json.arr(JsString("value1X"), JsString("value2X")))

    val optic = JsLens.each[String](__ \ "field1")

    optic.modify(_ + "X")(json) mustBe expectedJson
  }

  "Removing at a given path" must "prune the JSON at that path" in {
    val json: JsValue = Json.obj(
      "field1" -> Json.obj(
        "field1" -> JsString("value1"),
        "field2" -> JsString("value2")
      ),
      "field2" -> Json.obj(
        "field1" -> JsString("value3"),
        "field2" -> JsString("value4")
      )
    )
    val expectedJson = Json.obj(
      "field1" -> Json.obj(
        "field1" -> JsString("value1"),
        "field2" -> JsString("value2")
      )
    )

    import monocle.function.At._
    import JsLens._

    remove(__ \ "field2")(json) mustBe expectedJson
  }
}
