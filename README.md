# mod-linked-data
© 2024 EBSCO Information Services.

This software is distributed under the terms of the Apache License, Version 2.0.
See the file "[LICENSE](LICENSE)" for more information.
## Introduction
mod-linked-data manages the data graph of the library's catalog, providing REST APIs for CRUD operations on the data graph.
### Dependencies on libraries
This module is dependent on the following libraries:
- [lib-linked-data-dictionary](https://github.com/folio-org/lib-linked-data-dictionary)
- [lib-linked-data-fingerprint](https://github.com/folio-org/lib-linked-data-fingerprint)
- [lib-linked-data-marc4ld](https://github.com/folio-org/lib-linked-data-marc4ld)
## Compiling
```bash
mvn clean install
```
Skip tests:
```bash
mvn clean install -DskipTests
```
## Modes of Execution
mod-linked-data can be executed in standalone mode or as part of the FOLIO platform.
### FOLIO mode
This is default mode. In FOLIO mode, mod-linked-data integrates with other FOLIO modules:
- Receives MARC Authority records from [mod-source-record-storage](https://github.com/folio-org/mod-source-record-storage) via Kafka topics and updates the linked data graph.
- Sends linked data graph updates to the [mod-search](https://github.com/folio-org/mod-search) module via Kafka topics.
- Sends linked data graph updates to the [mod-inventory](https://github.com/folio-org/mod-inventory) module via Kafka topics.

To run mod-linked-data in FOLIO mode, do not set the environment variable `SPRING_PROFILES_ACTIVE`.
### Standalone mode
In standalone mode, mod-linked-data operates independently without communication with other FOLIO modules.
In this mode, you can create and update graph using REST APIs. However, the graph will not be synced with other FOLIO modules.

To run mod-linked-data in standalone mode, set the value of the environment variable `SPRING_PROFILES_ACTIVE` to `standalone`.
## Environment Variables
| Name                                                | Default Value                   | Description                                                                                                                                                                           |
|-----------------------------------------------------|---------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SERVER_PORT                                         | 8081                            | The port number of the application                                                                                                                                                    |
| ENV                                                 | folio                           | The logical name of the deployment, must be unique across all environments using the same shared Kafka/Elasticsearch clusters, `a-z (any case)`, `0-9`, `-`, `_` symbols only allowed |
| DB_HOST                                             | localhost                       | Postgres hostname                                                                                                                                                                     |
| DB_PORT                                             | 5432                            | Postgres port                                                                                                                                                                         |
| DB_USERNAME                                         | postgres                        | Postgres username                                                                                                                                                                     |
| DB_PASSWORD                                         | postgres                        | Postgres password                                                                                                                                                                     |
| DB_DATABASE                                         | postgres                        | Postgres database name                                                                                                                                                                |
| SPRING_PROFILES_ACTIVE                              | -                               | Indicates if the application has to be run in FOLIO mode or standalone mode                                                                                                           |
| KAFKA_HOST`*`                                       | kafka                           | Kafka broker hostname                                                                                                                                                                 |
| KAFKA_PORT`*`                                       | 9092                            | Kafka broker port                                                                                                                                                                     |
| KAFKA_SECURITY_PROTOCOL`*`                          | PLAINTEXT                       | Kafka security protocol used to communicate with brokers (SSL or PLAINTEXT)                                                                                                           |
| KAFKA_SSL_KEYSTORE_LOCATION`*`                      | -                               | The location of the Kafka key store file. This is optional for client and can be used for two-way authentication for client.                                                          |
| KAFKA_SSL_KEYSTORE_PASSWORD`*`                      | -                               | The store password for the Kafka key store file. This is optional for client and only needed if 'ssl.keystore.location' is configured.                                                |
| KAFKA_SSL_TRUSTSTORE_LOCATION`*`                    | -                               | The location of the Kafka trust store file.                                                                                                                                           |
| KAFKA_SSL_TRUSTSTORE_PASSWORD`*`                    | -                               | The password for the Kafka trust store file. If a password is not set, trust store file configured will still be used, but integrity checking is disabled.                            |
| KAFKA_CONSUMER_MAX_POLL_RECORDS`*`                  | 200                             | Maximum number of records returned in a single call to poll().                                                                                                                        |
| KAFKA_SOURCE_RECORD_DOMAIN_EVENT_TOPIC_PATTERN`*`   | (ENV\.)(.*\.)srs.source_records | Custom Source Record Domain Event topic name pattern                                                                                                                                  |
| KAFKA_SOURCE_RECORD_DOMAIN_EVENT_CONCURRENCY`*`     | 1                               | Custom number of kafka concurrent threads for Source Record Domain Event message consuming.                                                                                           |
| KAFKA_INVENTORY_INSTANCE_EVENT_TOPIC_PATTERN`*`     | (ENV\.)(.*\.)inventory.instance | Custom Inventory Instance Event topic name pattern                                                                                                                                    |
| KAFKA_INVENTORY_INSTANCE_EVENT_CONCURRENCY`*`       | 1                               | Custom number of kafka concurrent threads for Inventory Instance message consuming.                                                                                                   |
| KAFKA_RETRY_INTERVAL_MS`*`                          | 2000                            | Specifies time to wait before reattempting message processing.                                                                                                                        |
| KAFKA_RETRY_DELIVERY_ATTEMPTS`*`                    | 6                               | Specifies how many queries attempt to perform after the first one failed.                                                                                                             |
| KAFKA_WORK_SEARCH_INDEX_TOPIC`*`                    | linked-data.work                | Custom Work Search Index topic name                                                                                                                                                   |
| KAFKA_WORK_SEARCH_INDEX_TOPIC_PARTITIONS`*`         | 1                               | Custom Work Search Index topic partitions number                                                                                                                                      |
| KAFKA_WORK_SEARCH_INDEX_TOPIC_REPLICATION_FACTOR`*` | -                               | Custom Work Search Index topic replication factor                                                                                                                                     |
| KAFKA_INVENTORY_INSTANCE_INGRESS_EVENT_TOPIC`*`     | inventory.instance_ingress      | Custom Inventory Instance Ingress Event topic name                                                                                                                                    |
* Applicable only in FOLIO mode
## REST API
Full list of APIs are documented in [src/main/resources/swagger.api/mod-linked-data.yaml](https://github.com/folio-org/mod-linked-data/blob/master/src/main/resources/swagger.api/mod-linked-data.yaml).
Details of few important APIs are provided below.
### Creating Work resources
At present, API support creation of Work resource (with many other resource types included, like Instance).

Resources can be created by making a POST request to the `/resource` endpoint.
Refer [src/main/resources/swagger.api/schema/resourceRequestDto.json](https://github.com/folio-org/mod-linked-data/blob/master/src/main/resources/swagger.api/schema/resourceRequestDto.json) for the schema of the request body.
#### Example request for creating a Work resource:
```bash
curl --location '{{ base-uri }}/resource' \
--header 'x-okapi-tenant: {{ tenant identifier }}' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'x-okapi-token: {{ token }} \
--data '{
    "resource": {
    "http://bibfra.me/vocab/lite/Work":{
      "http://bibfra.me/vocab/marc/title":[
        {
          "http://bibfra.me/vocab/marc/Title":{
            "http://bibfra.me/vocab/bflc/nonSortNum":[
              "4"
            ],
            "http://bibfra.me/vocab/marc/mainTitle":[
              "The main title"
            ],
            "http://bibfra.me/vocab/marc/partNumber":[
              "1"
            ],
            "http://bibfra.me/vocab/marc/partName":[
              "part 1"
            ]
          }
        }
      ],
      "http://bibfra.me/vocab/marc/governmentPublication":[
        {
          "http://bibfra.me/vocab/marc/term":[
            "Government"
          ],
          "http://bibfra.me/vocab/lite/link":[
            "http://id.loc.gov/vocabulary/mgovtpubtype/g"
          ]
        }
      ],
      "http://bibfra.me/vocab/lite/dateStart":[
        "2024"
      ],
      "http://bibfra.me/vocab/marc/originPlace":[
        {
          "http://bibfra.me/vocab/lite/label":[
            "United States"
          ],
          "http://bibfra.me/vocab/lite/name":[
            "United States"
          ],
          "http://bibfra.me/vocab/lite/link":[
            "http://id.loc.gov/vocabulary/countries/xxu"
          ]
        }
      ],
      "http://bibfra.me/vocab/marc/targetAudience":[
        {
          "http://bibfra.me/vocab/marc/term":[
            "Preschool"
          ],
          "http://bibfra.me/vocab/lite/link":[
            "http://id.loc.gov/vocabulary/maudience/pre"
          ]
        }
      ],
      "http://bibfra.me/vocab/marc/tableOfContents":[
        "Table of contents"
      ],
      "http://bibfra.me/vocab/marc/summary":[
        "Summary note"
      ],
      "http://bibfra.me/vocab/lite/classification":[
        {
          "http://bibfra.me/vocab/marc/code":[
            "Lib-Congress-number"
          ],
          "http://bibfra.me/vocab/marc/source":[
            "lc"
          ],
          "http://bibfra.me/vocab/marc/itemNumber": [
            "Lib-Congress-number-item"
          ],
          "http://bibfra.me/vocab/marc/status": [
            {
              "http://bibfra.me/vocab/lite/label": [
                "used by assigner"
              ],
              "http://bibfra.me/vocab/lite/link": [
                "http://id.loc.gov/vocabulary/mstatus/uba"
              ]
            }
          ]
        },
        {
          "http://bibfra.me/vocab/marc/code":[
            "Dewey-number"
          ],
          "http://bibfra.me/vocab/marc/source":[
            "ddc"
          ],
          "http://bibfra.me/vocab/marc/itemNumber": [
            "Dewey-number-item"
          ],
          "http://bibfra.me/vocab/marc/editionNumber": [
            "Dewey-number-editionNumber"
          ],
          "http://bibfra.me/vocab/marc/edition": [
            "Dewey-number-edition"
          ]
        }
      ],
      "http://bibfra.me/vocab/marc/content":[
        {
          "http://bibfra.me/vocab/marc/term":[
            "cartographic image"
          ],
          "http://bibfra.me/vocab/lite/link":[
            "http://id.loc.gov/vocabulary/contentTypes/cri"
          ]
        }
      ],
      "http://bibfra.me/vocab/lite/language":[
        {
          "http://bibfra.me/vocab/marc/term": [
            "English"
          ],
          "http://bibfra.me/vocab/lite/link": [
            "http://id.loc.gov/vocabulary/languages/eng"
          ]
        }
      ],
      "_notes":[
        {
          "type":[
            "http://bibfra.me/vocab/marc/bibliographyNote"
          ],
          "value":[
            "Bib note"
          ]
        },
        {
          "type":[
            "http://bibfra.me/vocab/marc/languageNote"
          ],
          "value":[
            "Language note"
          ]
        },
        {
          "type":[
            "http://bibfra.me/vocab/lite/note"
          ],
          "value":[
            "General note"
          ]
        }
      ],
      "http://bibfra.me/vocab/scholar/dissertation": [
        {
          "http://bibfra.me/vocab/lite/label": [
            "label"
          ],
          "http://bibfra.me/vocab/marc/degree": [
            "degree"
          ],
          "http://bibfra.me/vocab/marc/dissertationYear": [
            "dissertation year"
          ],
          "http://bibfra.me/vocab/marc/dissertationNote": [
            "dissertation note"
          ],
          "http://bibfra.me/vocab/marc/dissertationID": [
            "dissertation id"
          ]
        }
      ]
    }
    }
}'
```
### Updating resources
Resource can be updated by making a PUT request to the `/resource/{id}` endpoint. At present, only Work resources can be updated through the API.
Refer [src/main/resources/swagger.api/schema/resourceRequestDto.json](https://github.com/folio-org/mod-linked-data/blob/master/src/main/resources/swagger.api/schema/resourceRequestDto.json) for the schema of the request body.
### Viewing a graph node
A graph node can be viewed by making a GET request to the `graph/resource/{id}` endpoint. Any kind of resources can be retrieved through this API.

```bash
curl --location '{{ base-uri }}/graph/resource/{id}' \
--header 'x-okapi-tenant: {tenant}' \
--header 'x-okapi-token: {token}'
```
# Integration with FOLIO
When running in FOLIO mode, this module integrates with multiple Folio modules via Kafka.
## Search module
The Linked Data module pushes a new and updated Work resource descriptions to the Kafka topic defined by KAFKA_WORK_SEARCH_INDEX_TOPIC env variable.
The [mod-search](https://github.com/folio-org/mod-search) module consumes message and indexes a Work resource descriptions in [OpenSearch](https://aws.amazon.com/opensearch-service/) index for search and retrieval.
## Source Record Storage module
The [mod-source-record-storage](https://github.com/folio-org/mod-source-record-storage) module pushes Source Record domain events to the topic defined by KAFKA_SOURCE_RECORD_DOMAIN_EVENT_TOPIC_PATTERN env variable.
The Linked Data module consumes a message and creates/updates a corresponding Authority resource in Linked Data graph.
## Inventory module
1. The Linked Data module pushes a new and updated Instance resource descriptions to the Kafka topic defined by KAFKA_INVENTORY_INSTANCE_INGRESS_EVENT_TOPIC env variable.
The [mod-inventory](https://github.com/folio-org/mod-inventory) module consumes message and creates/updates a corresponding Instance resource in the Inventory storage,
plus sends according message to the [mod-source-record-storage](https://github.com/folio-org/mod-source-record-storage) to make it create/update according source record.
2. The [mod-inventory](https://github.com/folio-org/mod-inventory) module pushes Inventory Instance events to the topic defined by KAFKA_INVENTORY_INSTANCE_EVENT_TOPIC_PATTERN env variable.
The Linked Data module consumes a message and updates a corresponding Instance resource in Linked Data graph, but only regarding suppression flags.

# Possible error responses
All error responses are listed in [src/main/resources/errors.yml](https://github.com/folio-org/mod-linked-data/blob/master/src/main/resources/errors.yml).
Validation error could contain various message codes listed in [src/main/resources/validationMessages.properties](https://github.com/folio-org/mod-linked-data/blob/master/src/main/resources/validationMessages.properties).
