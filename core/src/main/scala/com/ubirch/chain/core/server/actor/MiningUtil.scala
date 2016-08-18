package com.ubirch.chain.core.server.actor

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.backend.chain.model.FullBlock
import com.ubirch.chain.core.config.{Config, ConfigKeys}
import com.ubirch.chain.core.merkle.BlockUtil
import com.ubirch.client.storage.ChainStorageServiceClient._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-08-16
  */
class MiningUtil extends LazyLogging {

  def blockCheck(): Unit = {

    sizeCheck() map {

      case true => mine()

      case false =>

        ageCheck() map {
          case true => mine()
          case false => // do nothing
        }

    }

  }

  private def sizeCheck(): Future[Boolean] = {

    val blockMaxSizeKb = Config.blockMaxSize
    logger.debug(s"checking size of unmined hashes: ${ConfigKeys.BLOCK_MAX_SIZE} = $blockMaxSizeKb kb")

    unminedHashes() map { hashes =>

      val size = BlockUtil.size(hashes.hashes)
      val sizeKb = size / 1000
      val maxBlockSizeBytes = Config.blockMaxSize * 1000

      size >= maxBlockSizeBytes match {

        case true =>
          logger.info(s"trigger mining of a new block (trigger: size) -- ${hashes.hashes.length} hashes ($sizeKb kb)")
          true

        case false => false

      }

    }

  }

  private def ageCheck(): Future[Boolean] = {

    mostRecentBlock() map {

      case None =>

        logger.error("found no most recent block")
        false

      case Some(block) =>

        val nextCreationDate = block.created.plusSeconds(Config.mineEveryXSeconds)

        nextCreationDate.isBeforeNow match {

          case true =>
            logger.info("start mining a new block (trigger: time)")
            true

          case false =>
            logger.debug("don't have to mine a new block yet based on time trigger")
            false

        }

    }

  }

  private def mine(): Future[Option[FullBlock]] = {

    mostRecentBlock() flatMap {

      case None =>
        logger.error("found no most recent block")
        Future(None)

      case Some(mostRecentBlock) =>

        unminedHashes() flatMap { unmined =>

          val previousBlockHash = mostRecentBlock.hash
          val hashes = unmined.hashes

          val newBlock = BlockUtil.newBlock(previousBlockHash, hashes)
          val blockHash = newBlock.hash
          logger.info(s"new block hash: $blockHash (blockSize=${BlockUtil.size(hashes) / 1000} kb; ${hashes.size} hashes)")

          upsertFullBlock(newBlock) flatMap  {

            case None =>
              logger.error("failed to insert new block")
              Future(None)

            case Some(upsertedBlock) =>
              // TODO FIXME switch to Seq instead of Set !!!
              deleteHashes(hashes.toSet) map {

                case true => Some(upsertedBlock)

                case false =>
                  logger.error(s"failed to newly mined hashes from unmined list: newBlock.hash=${upsertedBlock.hash}")
                  Some(upsertedBlock)

              }

          }

        }

    }

  }

}
