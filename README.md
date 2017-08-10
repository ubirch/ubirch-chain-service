# ubirch-chain-service

## General Information

TODO


## Release History

### Version 0.1.5 (tbd)

* `AWS_QUEUE_OWNER_ID` is now optional in _application.docker.conf_

### Version 0.1.4 (2017-07-27)

* update to `com.ubirch.util:json:0.4.3`
* update to `com.ubirch.util:deep-check-model:0.1.3`
* update to `com.ubirch.util:response-util:0.2.4`
* update to `com.ubirch.util:mongo-test-utils:0.3.5`
* update to `com.ubirch.util:mongo-utils:0.3.5`

### Version 0.1.3 (2017-07-18)

* update to Akka 2.4.19
* update _com.ubirch.util:mongo(-test)-utils_ to 0.3.4

### Version 0.1.2 (2017-07-17)

* fixed faulty json in curl example
* update Akka HTTP to 10.0.9
* update _com.ubirch.util:rest-akka-http(-test)_ to 0.3.8
* update _com.ubirch.util:response-util_ to 0.2.3

### Version 0.1.1 (2017-07-12)

* update logging dependencies
* update logback configs
* delete unused config key
* update application.docker.conf
* update _com.ubirch.util:mongo(-test)-utils_ to 0.3.3

### Version 0.1.0 (2017-06-30)

* initial release


## Scala Dependencies

### `cmdtools`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.chain" %% "cmdtools" % "0.1.5-SNAPSHOT"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.chain" %% "config" % "0.1.5-SNAPSHOT"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("rick-beton", "maven"), // needed for the notary-client
  Resolver.bintrayRepo("hseeberger", "maven")
)
libraryDependencies ++= Seq(
  "com.ubirch.chain" %% "core" % "0.1.5-SNAPSHOT"
)
```

### `model-db`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.chain" %% "model-db" % "0.1.5-SNAPSHOT"
)
```

### `model-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("hseeberger", "maven")
)
libraryDependencies ++= Seq(
  "com.ubirch.chain" %% "model-rest" % "0.1.5-SNAPSHOT"
)
```

### `server`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("hseeberger", "maven")
)
libraryDependencies ++= Seq(
  "com.ubirch.chain" %% "server" % "0.1.5-SNAPSHOT"
)
```

### `test-tools`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("hseeberger", "maven")
)
libraryDependencies ++= Seq(
  "com.ubirch.chain" %% "test-tools" % "0.1.5-SNAPSHOT"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.chain" %% "util" % "0.1.5-SNAPSHOT"
)
```


## REST Methods

### Welcome / Health / Check

    curl localhost:8100/
    curl localhost:8100/api/chainService/v1
    curl localhost:8100/api/chainService/v1/check

If healthy the server response is:

    200 {"version":"1.0","status":"OK","message":"Welcome to the ubirchChainService ( $GO_PIPELINE_NAME / $GO_PIPELINE_LABEL / $GO_PIPELINE_REVISION )"}

If not healthy the server response is:

    400 {"version":"1.0","status":"NOK","message":"$ERROR_MESSAGE"}

### Deep Check / Server Health

    curl localhost:8100/api/chainService/v1/deepCheck

If healthy the response is:

    200 {"status":true,"messages":[]}

If not healthy the status is `false` and the `messages` array not empty:

    503 {"status":false,"messages":["unable to connect to the database"]}

### Transaction

#### DeviceData

Writes a device message to the blockchain (where _payload_ is any valid JSON and _id_ is the external id):

    curl -i -XPOST localhost:8080/api/v1/chainService/tx/deviceMsg -H "Content-Type: application/json" -d '{"id": "92b72011-c458-447f-90c5-69ea26e22cf8", "payload": {"t": 29.067}}'

#### DeviceDataHash

Writes a hash to the blockchain (where _hash_ is any String and _id_ is the external id):

    curl -i -XPOST localhost:8080/api/v1/chainService/tx/deviceMsgHash -H "Content-Type: application/json" -d '{"id": "35b3f02d-c9c4-4139-8fe6-6857daeb8b8b", "hash": "e9758380e3f9d2d0b9e0b13e424fcbf94a576c59dcf136b201832d1a687efc86"}'


## Configuration

TODO


## Deployment Notes

TODO


## Automated Tests

run all tests

    ./sbt test

### generate coverage report

    ./sbt coverageReport

more details here: https://github.com/scoverage/sbt-scoverage


## Local Setup

1. Installation
 
  1.1. Dependencies

    * Ubuntu 16.04

      ```
      sudo apt install python3-pip libssl-dev
      sudo -H pip3 install -r bigChainDbStore/requirements.txt
      ```

    * MacOS

      ```
      tbd
      ```

  1.2. BigchainDB

    [BigchainDB Quickstart](https://docs.bigchaindb.com/projects/server/en/latest/quickstart.html)

2. Update

  2.1 BigchainDB

    sudo -H pip3 install --upgrade bigchaindb bigchaindb_driver

3. Start Environment

  3.1. BigchainDB
  
    Follow the instruction in: [BigchainDB Quickstart](https://docs.bigchaindb.com/projects/server/en/latest/quickstart.html).

  3.2. Python Wrapper around BigchainDB

    This Python wrapper listens on a queue for new transactions to store in BigchainDB.
    
    ```
    export AWS_ACCESS_KEY_ID={YOUR AWS ACCESS KEY}
    export AWS_SECRET_ACCESS_KEY={YOUR AWS SECRET KEY}
    python3 bigChainDbStore/src/bigChainDbStore.py
    ```

  3.3. Start Server

    ```
    export AWS_ACCESS_KEY_ID={YOUR AWS ACCESS KEY}
    export AWS_SECRET_ACCESS_KEY={YOUR AWS SECRET KEY}
    ./sbt server/run
    ```

4. Generate Test Data

  4.1. Directly to BigchainDB
  
    ```
    export AWS_ACCESS_KEY_ID={YOUR AWS ACCESS KEY}
    export AWS_SECRET_ACCESS_KEY={YOUR AWS SECRET KEY}
    python3 bigChainDbStore/src/bigChainDbTester.py
    ```

  4.2. Device Data Through chain-service
  
    ```
    export AWS_ACCESS_KEY_ID={YOUR AWS ACCESS KEY}
    export AWS_SECRET_ACCESS_KEY={YOUR AWS SECRET KEY}
    python3 bigChainDbStore/src/chainServiceDeviceMsgTester.py
    ```


## Create Docker Image

    ./goBuild.sh assembly
