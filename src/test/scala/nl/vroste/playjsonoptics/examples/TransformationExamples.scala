package nl.vroste.playjsonoptics.examples

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

import monocle.function.At._
import nl.vroste.playjsonoptics.Helpers._
import nl.vroste.playjsonoptics.JsLens
import nl.vroste.playjsonoptics.JsLens.jsAt
import org.scalatest.{FlatSpec, Inside, MustMatchers}
import play.api.libs.json._

/**
  * Series of examples for how to use PlayJsonOptics to transform JSON
  */
class TransformationExamples extends FlatSpec with MustMatchers with Inside {

  val jsonWithField =
    Json.parse(
      s"""
    {
        "time": "2018-01-30T12:30:24Z",
        "estimateOrActual": "ESTIMATE"
    }""")

  val jsonWithoutField =
    Json.parse(
      s"""
    {
        "time": "2018-01-30T12:30:24Z"
    }""")


  it must "modify an optional field" in {
    val modify = JsLens[String](__ \ "estimateOrActual") modify (_ + "V2")

    val result = modify(jsonWithField)
    inside(result) {
      case o: JsObject =>
        o("estimateOrActual") mustBe JsString("ESTIMATEV2")
    }

    val result2 = modify(jsonWithoutField)
    inside(result2) {
      case o: JsObject =>
        o.value must not contain key("estimateOrActual")
    }
  }

  it must "set a default value if a field is missing" in {
    val modify = (__ \ "estimateOrActual").setDefault[String]("DEFAULT")

    val result = modify(jsonWithoutField.as[JsObject])
    withClue(result) {
      inside(result) {
        case o: JsObject =>
          o("estimateOrActual") mustBe JsString("DEFAULT")
      }
    }

    val result2 = modify(jsonWithField.as[JsObject])
    withClue(result2) {
      inside(result2) {
        case o: JsObject =>
          o("estimateOrActual") mustBe JsString("ESTIMATE")
      }
    }
  }

  it must "work with values in a custom type when a Format for that type is available" in {
    val json = Json.parse(
      s""" { "times": [
         |      {
         |       "time": "2018-01-30T12:30:24Z"
         |      },
         |      {
         |       "time": "2018-01-30T12:30:24Z",
         |       "estimateOrActual": "ESTIMATE"
         |      }
         |  ]
         |}
       """.stripMargin)

    val modify = (JsLens.each[JsValue](__ \ "times")
      composeOptional JsLens[Instant](__ \ "time")
      modify (_.plus(1, DAYS)))

    val result = modify(json)
  }


  it must "remove a field" in {

    val result = remove(__ \ "estimateOrActual")(jsonWithField)

    withClue(result) {
      inside(result) {
        case o: JsObject =>
          o.value must not contain key("estimateOrActual")
      }
    }

    val result2 = remove(__ \ "estimateOrActual")(jsonWithoutField)
    withClue(result2) {
      inside(result2) {
        case o: JsObject =>
          o.value must not contain key("estimateOrActual")
      }
    }
  }

  it must "change the type of a field" in {
    val modify = JsLens[JsValue](__ \ "estimateOrActual").modify {
      case JsString(e) => JsBoolean(e == "ESTIMATE")
      case _ => JsBoolean(false)
    }

    val result = modify(jsonWithField.as[JsObject])
    withClue(result) {
      inside(result) {
        case o: JsObject =>
          o.value("estimateOrActual") mustBe JsBoolean(true)
      }
    }

    val result2 = modify(jsonWithoutField.as[JsObject])
    withClue(result2) {
      inside(result2) {
        case o: JsObject =>
          o.value must not contain key("estimateOrActual")
      }
    }

  }

  val arrayOfTimesJson = Json.parse(
    s""" { "times": [
       |      {
       |       "time": "2018-01-30T12:30:24Z"
       |      },
       |      {
       |       "time": "2018-01-30T12:30:24Z",
       |       "estimateOrActual": "ESTIMATE"
       |      }
       |  ]
       |}
       """.stripMargin)

  it must "apply transformations to an array of structures" in {
    val modify = JsLens.each[JsValue](__ \ "times") composeOptional JsLens[String](__ \ "estimateOrActual") modify (_ + "V2")

    val result = modify(arrayOfTimesJson)
  }

  it must "apply transformations to an array of structures that satisfy some condition" in {
    val modify = JsLens.each[JsValue](__ \ "times") composeOptional JsLens[String](__ \ "estimateOrActual") modify (_ + "V2")

    val result = modify(arrayOfTimesJson)
  }

  it must "rename a field" in {
    val moveField = (__ \ "times") moveTo (__ \ "nested" \ "datetimes")

    val result = moveField(arrayOfTimesJson)

    withClue(Json.prettyPrint(result)) {
      (__ \ "nested" \ "datetimes").asSingleJsResult(result).asOpt mustBe defined
    }
  }
}
