# mod-linked-data
© 2024 EBSCO Information Services.

This software is distributed under the terms of the Apache License, Version 2.0.
See the file "[LICENSE](LICENSE)" for more information.

## Introduction
mod-linked-data manages the data graph of the library's catalog, providing REST APIs for CRUD operations on the data graph.

### Dependencies on libraries
This module is dependent on the following libraries:
- [lib-linked-data-marc4ld](https://github.com/FOLIO-EIS/lib-linked-data-marc4ld)
- [lib-linked-data-fingerprint](https://github.com/FOLIO-EIS/lib-linked-data-fingerprint)
- [lib-linked-data-dictionary](https://github.com/FOLIO-EIS/lib-linked-data-dictionary)

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
In FOLIO mode, mod-linked-data integrates with other FOLIO modules:

- Receives MARC Bibliographic records from [SRS](https://github.com/folio-org/mod-source-record-storage) via Kafka topics and updates the linked data graph.
- Sends linked data graph updates to the [mod-search-ld](https://github.com/FOLIO-EIS/mod-search-ld) module via Kafka topics.
- (Future) Receives MARC Authority records from [SRS](https://github.com/folio-org/mod-source-record-storage) via Kafka topics and updates the linked data graph.
- (Future) Pushes linked data graph updates to the [mod-inventory](https://github.com/folio-org/mod-inventory) module via Kafka topics.

To run mod-linked-data in FOLIO mode, set the value of the environment variable `spring.profiles.active` to `folio,search`.


### Standalone mode
In standalone mode, mod-linked-data operates independently without communication with other FOLIO modules.
In this mode, you can create and update graph using REST APIs. However, the graph will not be synced with other FOLIO modules.

To run mod-linked-data in standalone mode, set the value of the environment variable `spring.profiles.active` to `local`.

## Environment Variables
| Name                                 | Default Value | Description                                                                                                                                                |
|--------------------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DB_HOST                              | localhost     | Postgres hostname                                                                                                                                          |
| DB_PORT                              | 5432          | Postgres port                                                                                                                                              |
| DB_USERNAME                          | postgres      | Postgres username                                                                                                                                          |
| DB_PASSWORD                          | postgres      | Postgres password                                                                                                                                          |
| DB_DATABASE                          | postgres      | Postgres database name                                                                                                                                     |
| server.port                          | 8081          | Server port                                                                                                                                                |
| spring.profiles.active               | -             | Indicates if the application has to be run in FOLIO mode or standalone mode                                                                                |
| KAFKA_HOST `*`                       | kafka         | Kafka broker hostname                                                                                                                                      |
| KAFKA_PORT `*`                       | 9092          | Kafka broker port                                                                                                                                          |
| KAFKA_SECURITY_PROTOCOL `*`          | PLAINTEXT     | Kafka security protocol used to communicate with brokers (SSL or PLAINTEXT)                                                                                |
| KAFKA_SSL_KEYSTORE_LOCATION `*`      | -             | The location of the Kafka key store file. This is optional for client and can be used for two-way authentication for client.                               |
| KAFKA_SSL_KEYSTORE_PASSWORD `*`      | -             | The store password for the Kafka key store file. This is optional for client and only needed if 'ssl.keystore.location' is configured.                     |
| KAFKA_SSL_TRUSTSTORE_LOCATION `*`    | -             | The location of the Kafka trust store file.                                                                                                                |
| KAFKA_SSL_TRUSTSTORE_PASSWORD `*`    | -             | The password for the Kafka trust store file. If a password is not set, trust store file configured will still be used, but integrity checking is disabled. |
| KAFKA_CONSUMER_MAX_POLL_RECORDS `*`  | 200           | Maximum number of records returned in a single call to poll().                                                                                             |

* Applicable only in FOLIO mode

## REST APIs

Full list of APIs are documented in [src/main/resources/swagger.api/mod-linked-data.yaml](src/main/resources/swagger.api/mod-linked-data.yaml).

Details of few important APIs are provided below.

### Creating Work & Instance resources
At present, API support creation of Work and Instance resources.

Resources can be created by making a POST request to the `/resource` endpoint.
Refer [src/main/resources/swagger.api/schema/resourceDto.json](src/main/resources/swagger.api/schema/resourceDto.json) for the schema of the request body.

#### Example request for creating an Instance resource:
```bash
curl --location '{{ base-uri }}/resource' \
--header 'x-okapi-tenant: {{ tenant identifier }}' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'x-okapi-token: {{ token }} \
--data '{
    "resource": {
        "http://bibfra.me/vocab/lite/Instance": {
            "http://bibfra.me/vocab/marc/title": [
                {
                    "http://bibfra.me/vocab/marc/Title": {
                        "http://bibfra.me/vocab/bflc/nonSortNum": [
                            "0"
                        ],
                        "http://bibfra.me/vocab/marc/mainTitle": [
                            "Main title"
                        ],
                        "http://bibfra.me/vocab/marc/subTitle": [
                            "Sub title"
                        ]
                    }
                }
            ],
            "http://bibfra.me/vocab/marc/publication": [
                {
                    "http://bibfra.me/vocab/lite/providerDate": [
                        "1981"
                    ],
                    "http://bibfra.me/vocab/lite/providerPlace": [
                        {
                            "http://bibfra.me/vocab/lite/label": [
                                "Italy"
                            ],
                            "http://bibfra.me/vocab/lite/link": [
                                "http://id.loc.gov/vocabulary/countries/it"
                            ]
                        }
                    ],
                    "http://bibfra.me/vocab/lite/place": [
                        "Rome"
                    ],
                    "http://bibfra.me/vocab/lite/name": [
                        "Publisher Name"
                    ],
                    "http://bibfra.me/vocab/lite/date": [
                        "1981"
                    ]
                }
            ],
            "http://library.link/vocab/map": [
                {
                    "http://library.link/identifier/LCCN": {
                        "http://bibfra.me/vocab/lite/name": [
                            "80021016"
                        ],
                        "http://bibfra.me/vocab/marc/status": [
                            {
                                "http://bibfra.me/vocab/lite/label": [
                                    "current"
                                ],
                                "http://bibfra.me/vocab/lite/link": [
                                    "http://id.loc.gov/vocabulary/mstatus/current"
                                ]
                            }
                        ]
                    }
                }
            ],
            "http://bibfra.me/vocab/marc/media": [
                {
                    "http://bibfra.me/vocab/marc/term": [
                        "projected"
                    ],
                    "http://bibfra.me/vocab/lite/link": [
                        "http://id.loc.gov/vocabulary/mediaTypes/g"
                    ]
                }
            ],
            "http://bibfra.me/vocab/marc/carrier": [
                {
                    "http://bibfra.me/vocab/marc/term": [
                        "card"
                    ],
                    "http://bibfra.me/vocab/lite/link": [
                        "http://id.loc.gov/vocabulary/carriers/no"
                    ]
                }
            ],
            "_workReference": [
                {
                    "id": "{{ ID of the work resource }}"
                }
            ]
        }
    }
}'
```

### Updating resources
Resource can be updated by making a PUT request to the `/resource/{id}` endpoint. At present, only Instance and Work
resources can be updated through the API.
Refer [src/main/resources/swagger.api/schema/resourceDto.json](src/main/resources/swagger.api/schema/resourceDto.json)
for the schema of the request body.

### Viewing a graph node
A graph node can be viewed by making a GET request to the `graph/resource/{id}` endpoint. Any kind of resources can be
retrieved through this API.

```bash
curl --location '{{ base-uri }}/graph/resource/{id}' \
--header 'x-okapi-tenant: {tenant}' \
--header 'x-okapi-token: {token}'
```

## Integration with FOLIO Inventory module
When running in FOLIO mode, this module listens to MARC bibliographic records from the Kafka topic
`{environment}.{tenantId}.DI_INVENTORY_INSTANCE_CREATED_READY_FOR_POST_PROCESSING`. The received MARC records are
converted to resource descriptions using the [lib-linked-data-marc4ld](https://github.com/FOLIO-EIS/lib-linked-data-marc4ld)
library and then persisted in the graph.

> **Note:** This process will be updated to integrate with [SRS](https://github.com/folio-org/mod-source-record-storage)
> module in Ramsons release.

## Integration with mod-search-ld module
When running in FOLIO mode, this module will push the new and updated Work resource descriptions to the Kafka topic
`{environment}.{tenantId}.search.bibframe` for consumption by [mod-search-ld](https://github.com/FOLIO-EIS/mod-search-ld)
module. `mod-search-ld` will index the Work resource descriptions in [OpenSearch](https://aws.amazon.com/opensearch-service/)
index for search and retrieval.
