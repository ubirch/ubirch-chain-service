package com.ubirch.chain.test.util

import com.ubirch.backend.chain.model.{FullBlock, GenesisBlock, HashRequest}
import com.ubirch.chain.config.Config
import com.ubirch.chain.share.merkle.BlockUtil
import com.ubirch.chain.share.util.{HashRouteUtil, MiningUtil}
import com.ubirch.client.storage.ChainStorageServiceClient
import com.ubirch.util.crypto.hash.HashUtil
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-08-22
  */
object BlockGenerator {

  private val miningUtil = new MiningUtil
  private val hashRouteUtil = new HashRouteUtil

  def createGenesisBlock(ageCheckResultsInTrue: Boolean = false): GenesisBlock = {

    val genesis = BlockUtil.genesisBlock()

    val genesisToPersist = ageCheckResultsInTrue match {

      case true =>
        val created = DateTime.now.minusSeconds(Config.mineEveryXSeconds + 10)
        genesis.copy(created = created)

      case false => genesis

    }

    val genesisBlock = Await.result(ChainStorageServiceClient.saveGenesisBlock(genesisToPersist), 2 seconds)
    Thread.sleep(300)

    genesisBlock.get

  }

  def generateMinedBlock(elementCount: Int = 5000): FullBlock = {

    val hashes = HashUtil.randomSha256Hashes(elementCount) map (HashRequest(_))
    hashes foreach hashRouteUtil.hash
    Thread.sleep(1000)

    Await.result(miningUtil.mine(), 5 seconds).get

  }

  def generateFullBlock(previousBlockHash: String,
                        previousBlockNumber: Int,
                        elementCount: Int = 1000,
                        created: DateTime = DateTime.now
                       ): FullBlock = {

    val hashes = HashUtil.randomSha256Hashes(elementCount)
    hashes map (HashRequest(_)) foreach hashRouteUtil.hash

    val currentBlockHash = BlockUtil.blockHash(hashes, previousBlockHash)
    val fullBlock = FullBlock(currentBlockHash, created, version = "1.0", previousBlockHash, previousBlockNumber + 1, Some(hashes))

    Await.result(ChainStorageServiceClient.upsertFullBlock(fullBlock), 2 seconds).get

  }

}
