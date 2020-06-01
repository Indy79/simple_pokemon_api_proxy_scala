package com.jbjoffre.poke.json

import com.jbjoffre.poke.domains.Pokemon
import spray.json.DefaultJsonProtocol

object JsonProtocol extends DefaultJsonProtocol {

  implicit val pokemonJsonFormat = jsonFormat5(Pokemon)

}
