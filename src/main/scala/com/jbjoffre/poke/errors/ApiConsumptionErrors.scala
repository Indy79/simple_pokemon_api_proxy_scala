package com.jbjoffre.poke.errors

sealed trait ApiConsumptionErrors extends Throwable

final case class ResourceNotFound(id: String) extends ApiConsumptionErrors
final case class ClientInternalServerError() extends ApiConsumptionErrors
final case class InternalServerError(t: Throwable) extends ApiConsumptionErrors
final case class DeserializationError(t: Throwable) extends ApiConsumptionErrors