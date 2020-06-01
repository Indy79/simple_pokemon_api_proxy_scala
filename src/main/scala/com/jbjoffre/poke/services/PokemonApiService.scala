package com.jbjoffre.poke.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.jbjoffre.poke.domains.Pokemon
import com.jbjoffre.poke.errors.{ClientInternalServerError, DeserializationError, ResourceNotFound}

import scala.concurrent.{ExecutionContext, Future}

class PokemonApiService()(implicit val as: ActorSystem, val ec: ExecutionContext) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.jbjoffre.poke.json.JsonProtocol._

  def getPokemonById(id: String): Future[Pokemon] = {
    Http().singleRequest(Get(s"https://pokeapi.co/api/v2/pokemon/${id}")) flatMap {
      case resp@HttpResponse(StatusCodes.OK, _, _, _) => Future.successful(resp)
      case HttpResponse(StatusCodes.NotFound, _, _, _) => Future.failed(ResourceNotFound(id))
      case HttpResponse(StatusCodes.InternalServerError, _, _, _) => Future.failed(ClientInternalServerError())
    } flatMap { resp =>
      Unmarshal(resp.entity).to[Pokemon] recoverWith {
        case exc => Future.failed(DeserializationError(exc))
      }
    }
  }

  def getPokemonByName(name: String): Future[Pokemon] = {
    getPokemonById(name)
  }

}
