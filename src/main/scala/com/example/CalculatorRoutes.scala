package com.example

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.example.CalculatorRegistry._

import scala.concurrent.Future

class CalculatorRoutes(calculatorRegistry: ActorRef[CalculatorRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def evaluate(expression: String): Future[EvaluateUserExpression] =
    calculatorRegistry.ask(Evaluate(expression, _))

  val calculatorRoutes: Route =
    pathPrefix("evaluate") {
      concat(
        pathEnd {
          concat(
            post {
              entity(as[UserExpression]) { request =>
                onSuccess(evaluate(request.expression)) { response =>
                  complete(response.expressionResult)
                }
              }
            })
        }
      )
    }
}
