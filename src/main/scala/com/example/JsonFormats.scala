package com.example

import spray.json.DefaultJsonProtocol

object JsonFormats {

  import DefaultJsonProtocol._

  implicit val expressionResultJsonFormat = jsonFormat3(ExpressionResult)
  implicit val userExpressionJsonFormat = jsonFormat1(UserExpression)
}
