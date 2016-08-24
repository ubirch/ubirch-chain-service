package com.ubirch.chain.share.util

import com.ubirch.backend.chain.model.HashRequest
import com.ubirch.chain.test.base.ElasticSearchSpec
import com.ubirch.client.storage.ChainStorageServiceClient
import com.ubirch.util.crypto.hash.HashUtil

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-08-17
  */
class HashRouteUtilSpec extends ElasticSearchSpec {

  private val hashRouteUtil = new HashRouteUtil

  feature("HashRouteUtil.hash") {

    scenario("invalid input: empty string") {

      // test
      val hashResponse = hashRouteUtil.hash(HashRequest(""))

      // verify
      hashResponse map (_ shouldBe None)
      Thread.sleep(500)

      ChainStorageServiceClient.unminedHashes() map { unmined =>
        unmined.hashes shouldBe 'isEmpty
      }

    }

    scenario("valid input -> hash is stored") {

      // prepare
      val input = HashRequest("""{"foo": {"bar": 42}}""")

      for {
        // test
        res <- hashRouteUtil.hash(input)
      } yield {

        // verify
        Thread.sleep(500)
        val expectedHash = HashUtil.sha256HexString(input.data)
        res shouldBe Some(HashRequest(expectedHash))

        ChainStorageServiceClient.unminedHashes() map { unmined =>
          val hashes = unmined.hashes
          hashes should have length 1
          hashes should contain(expectedHash)
        }

      }

    }

    scenario("send same input twice -> same hash stored twice") {

      // prepare
      val input = HashRequest("""{"foo": {"bar": 42}}""")

      for {
        // test: send input: 1st time
        res1 <- hashRouteUtil.hash(input)
        // test: send input: 2nd time
        res2 <- hashRouteUtil.hash(input)
      } yield {

        // verify
        val expectedHash = HashUtil.sha256HexString(input.data)
        res1 shouldBe Some(HashRequest(expectedHash))
        res2 shouldBe Some(HashRequest(expectedHash))

        Thread.sleep(500)
        ChainStorageServiceClient.unminedHashes() map { unmined =>

          val hashes = unmined.hashes
          hashes should have length 2
          hashes.head should be(expectedHash)
          hashes(1) should be(expectedHash)

        }

      }

    }

  }

}