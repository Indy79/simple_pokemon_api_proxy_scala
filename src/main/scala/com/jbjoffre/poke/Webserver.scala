package com.jbjoffre.poke

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.jbjoffre.poke.registries.PokemonRegistry
import com.jbjoffre.poke.routes.PokemonRoutes

import scala.util.Failure
import scala.util.Success

object WebServer {

  private def startHttpServer(routes: Route, system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    import system.executionContext

    val futureBinding = Http().bindAndHandle(routes, "localhost", 8080)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
  //#start-http-server
  def main(args: Array[String]): Unit = {
    //#server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val pokemonRegistryActor = context.spawn(PokemonRegistry(), "PokemonRegistryActor")
      context.watch(pokemonRegistryActor)

      val routes = new PokemonRoutes(pokemonRegistryActor)(context.system)
      startHttpServer(routes.pokemonRoutes, context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "pokeApiHttpServer")
  }

}