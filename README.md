### Database start (in Docker)

docker run -d --name indexer -e POSTGRES_USERNAME=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=indexer -p 5432:5432 postgres:latest
