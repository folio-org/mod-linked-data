# authority-duplicate-identification

## What the recipient needs

- Java 21 installed
- Docker Desktop installed and running

## Start the database

- Double-click `start-db.command`

This starts PostgreSQL on `localhost:5433`.

## Start the server

- Double-click `start-server.command`

The server starts in standalone mode on `http://localhost:8081`.

## Change the similarity threshold

- Open `start-server.command` in a text editor.
- Change `MOD_LINKED_DATA_MARC_AUTHORITY_SIMILARITY_THRESHOLD=0.90` to the value you want.
- Save the file, then start the server again.

## Check that it is running

Open this URL in a browser:

- `http://localhost:8081/admin/health`

## Check authority deduplication

- Make sure the database and server are already running.
- Open Terminal.
- Run the first request below. This should succeed because it is the first authority.
- Run the second request below. This should be rejected because it is very similar to the first.
- If you want to insert the second record anyway, run the force request below.

macOS already includes `curl`, so nothing extra needs to be installed.

First request:

```bash
curl --request POST \
  --header "Content-Type: application/json" \
  --data @sample-authority-1.json \
  http://localhost:8081/graph/import/from-marc-authority
```

Second request:

```bash
curl --request POST \
  --header "Content-Type: application/json" \
  --data @sample-authority-2.json \
  http://localhost:8081/graph/import/from-marc-authority
```

Force insert of the second request:

```bash
curl --request POST \
  --header "Content-Type: application/json" \
  --data @sample-authority-2.json \
  "http://localhost:8081/graph/import/from-marc-authority?force=true"
```

## Stop the database

- Double-click `stop-db.command`

## Reset the database to a blank state

- Stop the server if it is running.
- Double-click `reset-db.command`
- Double-click `start-db.command`
- Double-click `start-server.command`

## Notes

- The first startup may take a little longer because Liquibase creates the database schema.
- The database bundle includes the `pgvector` extension required by the application.
- The jar in this bundle is named `mod-linked-data.jar`.
- If macOS blocks a script the first time, right-click it and choose `Open`.
