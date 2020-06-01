package com.jbjoffre.poke.routes

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.util.Timeout
import com.jbjoffre.poke.errors.{ClientInternalServerError, ResourceNotFound}
import com.jbjoffre.poke.registries.PokemonRegistry
import com.jbjoffre.poke.registries.PokemonRegistry.{FailedGetPokemonResponse, GetPokemonById, GetPokemonResponse, PokemonResponse}

import scala.concurrent.Future

class PokemonRoutes(pokemonRegistry: ActorRef[PokemonRegistry.Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.jbjoffre.poke.json.JsonProtocol._

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("poke-api.routes.ask-timeout"))

  def getPokemonById(id: String): Future[PokemonResponse] = pokemonRegistry.ask(GetPokemonById(id, _))

  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case ResourceNotFound(id) =>
        println(s"The resource with id : $id not found")
        complete(HttpResponse(StatusCodes.NotFound, entity = s"$id NOT_FOUND"))
      case _: ClientInternalServerError =>
        extractUri { uri =>
          println(s"Request to $uri could not be handled normally")
          complete(HttpResponse(StatusCodes.InternalServerError, entity = "INTERNAL_SERVER_ERROR"))
        }
    }

  val pokemonRoutes: Route = Route.seal(pathPrefix("pokemon") {
    get {
      path(Segment) { id =>
        onSuccess(getPokemonById(id)) {
          case GetPokemonResponse(pokemon) => complete(pokemon)
          case FailedGetPokemonResponse(t) => failWith(t)
        }
      }
    }
  })
}
