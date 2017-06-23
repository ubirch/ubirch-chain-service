package com.ubirch.chain.core.actor

import com.ubirch.chain.core.manager.DeepCheckManager
import com.ubirch.util.deepCheck.model.{DeepCheckRequest, DeepCheckResponse}

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-07
  */
class DeepCheckActor extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case _: DeepCheckRequest =>
      val sender = context.sender()
      deepCheck() map (sender ! _)

    case _ => log.error("unknown message")

  }

  private def deepCheck(): Future[DeepCheckResponse] = DeepCheckManager.connectivityCheck()

}
