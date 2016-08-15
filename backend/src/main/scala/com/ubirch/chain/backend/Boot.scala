package com.ubirch.chain.backend

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.chain.backend.actor.{AnchorActor, AnchorNow, BlockCheck, GenesisActor, GenesisCheck, MiningActor}
import com.ubirch.chain.backend.config.Config
import com.ubirch.chain.backend.route.MainRoute

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-07-26
  */
object Boot extends App with LazyLogging {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val miningActor = system.actorOf(Props[MiningActor])
  private val anchorActor = system.actorOf(Props[AnchorActor])
  private val genesisActor = system.actorOf(Props[GenesisActor])

  logger.info("ubirchChainService started")

  implicit val timeout = Timeout(15 seconds)
  (genesisActor ? GenesisCheck) map {

    case Success(result) => logger.info(s"genesis block check successful")

    case Failure(failure) =>
      logger.error("failed to check if genesis block exists or create one if not")
      throw failure

  }

  val bindingFuture = start()
  scheduleMiningRelatedJobs()

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() = {
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
    }
  })

  def start(): Future[ServerBinding] = {

    val interface = Config.interface
    val port = Config.port
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    Http().bindAndHandle((new MainRoute).myRoute, interface, port)

  }

  private def scheduleMiningRelatedJobs(): Unit = {

    val scheduler = system.scheduler
    val blockCheckInterval = Config.blockCheckInterval
    scheduler.schedule(blockCheckInterval seconds, blockCheckInterval seconds, miningActor, new BlockCheck())

    val anchorInterval = Config.anchorInterval
    scheduler.schedule(10 seconds, anchorInterval seconds, anchorActor, new AnchorNow())

  }

}