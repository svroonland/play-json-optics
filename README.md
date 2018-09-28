[![CircleCI](https://circleci.com/gh/svroonland/play-json-optics.svg?style=svg)](https://circleci.com/gh/svroonland/play-json-optics)

# play-json-optics
Monocle optics for Play! JSON data types. 

## About 
This libary allows easy creation of complex, composable JSON-to-JSON transformations with `play-json`.

In event sourcing applications or JSON-based APIs, the need to be backwards compatible with older versions of JSON data often means 
writing complex transformations from the old to the new structure. Although [Play Json Transformers](https://www.playframework.com/documentation/2.6.x/ScalaJsonTransformers) 
offer support for this, the syntax is rather cumbersome, error prone and becomes unwieldly for even typical transformations like modifying optional fields.

The optics from [Monocle](http://julien-truffaut.github.io/Monocle/) offer very powerful abstractions to assist in transforming Json. This library brings the power of `monocle` to `play-json`, together with some convenience methods for expressing common transformations.

Things you can do with this library:
* modify the type of JSON values
* set default values for missing values
* modify values deep inside a JSON structure as instances of your own data types
* apply modifications on arrays of values
* rename fields
* move substructures within the JSON to a different path

Optics to any path within a JSON data structure can be expressed using `play-json`'s path syntax, eg `__ \ "user" \ "name"`.

## Usage
Add the following to your build.sbt:

```
resolvers += Resolver.bintrayRepo("vroste", "maven"),
libraryDependencies += "nl.vroste" % "play-json-optics_2.12" % "0.1.0.0"
```

## Example

### Change strings to booleans in an array of Json objects
```scala
import nl.vroste.playjsonoptics.JsLens
import play.api.libs.json._

val json = Json.parse(""" 
{
  "times": [
    {
      "time": "2018-01-30T12:30:24Z"
    },
    {
      "time": "2018-01-30T12:30:24Z",
      "estimateOrActual": "ESTIMATE"
    }
  ]
}""")

val modifyOne = JsLens[JsValue](__ \ "estimateOrActual").modify {
  case JsString(e) => JsBoolean(e == "ESTIMATE")
  case _           => JsBoolean(false)
}

val modifyAll = JsLens.each[JsValue](__ \ "times") composeOptional modifyOne

modifyAll(json) 
```
will produce: 
```json
{
  "times": [
    {
      "time": "2018-01-30T12:30:24Z"
    },
    {
      "time": "2018-01-30T12:30:24Z",
      "estimateOrActual": true
    }
  ]
}
```

### Transform with custom types
```scala
JsLens[Instant](__ \ "time") modify (_.plus(1, DAYS)))
```

### Set default value
```scala
(__ \ "estimateOrActual").setDefault[String]("DEFAULT")
```

### Move substructures
```scala
(__ \ "times") moveTo (__ \ "nested" \ "datetimes")
```
### More examples
See [TransformationExamples](https://github.com/svroonland/play-json-optics/blob/master/playjsonoptics/src/test/scala/com/vroste/playjsonoptics/examples/TransformationExamples.scala)

