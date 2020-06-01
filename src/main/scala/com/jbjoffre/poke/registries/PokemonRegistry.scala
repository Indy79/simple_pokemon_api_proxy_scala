package com.jbjoffre.poke.registries

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.jbjoffre.poke.domains.Pokemon
import com.jbjoffre.poke.services.PokemonApiService

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object PokemonRegistry {

  sealed trait Command

  final case class GetPokemonById(id: String, replyTo: ActorRef[PokemonResponse]) extends Command
  final case class GetPokemonByName(name: String, replyTo: ActorRef[PokemonResponse]) extends Command

  sealed trait PokemonResponse

  final case class GetPokemonResponse(pokemon: Pokemon) extends PokemonResponse
  final case class FailedGetPokemonResponse(t: Throwable) extends PokemonResponse

  def apply(): Behavior[Command] = Behaviors.setup[Command] { context =>
    implicit val classic: ActorSystem = context.system.classicSystem
    implicit val ec: ExecutionContextExecutor = context.executionContext
    val service = new PokemonApiService()
    Behaviors.receiveMessage {
      case GetPokemonById(id, replyTo) =>
        service getPokemonById id onComplete {
          case Success(pokemon) => replyTo ! GetPokemonResponse(pokemon)
          case Failure(exception) => replyTo ! FailedGetPokemonResponse(exception)
        }
        Behaviors.same
      case GetPokemonByName(name, replyTo) =>
        service getPokemonByName name onComplete {
          case Success(pokemon) => replyTo ! GetPokemonResponse(pokemon)
          case Failure(exception) => replyTo ! FailedGetPokemonResponse(exception)
        }
        Behaviors.same
    }
  }

}
