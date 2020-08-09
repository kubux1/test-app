package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.StatusCodes

final case class UserExpression(expression: String)

final case class ExpressionResult(statusCodes: Int, errorMsg: Option[String], result: Option[Double])

object CalculatorRegistry {

  // actor protocol
  sealed trait Command

  final case class Evaluate(expression: String, replyTo: ActorRef[EvaluateUserExpression]) extends Command

  final case class EvaluateUserExpression(expressionResult: Option[ExpressionResult])

  def apply(): Behavior[Command] = registry(Set.empty)

  val validCharacters: Set[Char] = (List('/', '*', '+', '-', '(', ')', ' ') ++ ('0' to '9')).toSet

  def isValidString(s: String): Boolean = s.forall(validCharacters.contains(_))

  private def registry(expressionResult: Set[ExpressionResult]): Behavior[Command] =
    Behaviors.receiveMessage {
      case Evaluate(expression, replyTo) =>
        var result: Option[Double] = None
        var statusCode: Int = StatusCodes.OK.intValue
        var statusMsg: Option[String] = None

        if (isValidString(expression)) {
          try {
            result = Some(ArithmeticParser.readExpression(expression).get())
            if (result.get.isInfinite) {
              result = None
              statusCode = StatusCodes.BadRequest.intValue
              statusMsg = Some("Cannot divide by 0")
            }
          } catch {
            case ex: NoSuchElementException =>
              statusCode = StatusCodes.BadRequest.intValue
              statusMsg = Some(ex.getMessage)
          }
        } else {
          statusCode = StatusCodes.BadRequest.intValue
          statusMsg = Some("String contains invalid characters, only following are permitted: " + validCharacters.toString())
        }

        replyTo ! EvaluateUserExpression(Some(ExpressionResult(statusCode, statusMsg, result)))
        Behaviors.same
    }
}
