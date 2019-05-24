### Database start (in Docker)

Disclaimer: We are using Spring Boot and could not get SQLite working with it. As a result the db dump is for the PostgreSQL database. 
The code also expects the PostgreSQL database. 

IMPORTANT: The database names had to be changed. The tables we want are "index_word" and "posting". Both included in the "indexer" schema and located in the "indexer" database.

You can start the necessary database by running the following command:

docker run -d --name indexer -e POSTGRES_USERNAME=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=indexer -p 5432:5432 postgres:latest

### Rest endpoints

* GET /loadFiles - creates inverted index
* POST /search - query via inverted index
* POST /search-slowly - query naively

For the POST endpoints you need to provide a payload:

    {
        query: "social services" 
    }

Using this payload will result in the above query being queried.

### Make it run

1. Install docker, maven, java 1.8
2. Run the database with the command above
3. Move into the /indexer folder and run `mvn clean package`
4. While in the /indexer folder run the program with `java -jar target/indexer-0.0.1.jar`
5. Use endpoints at the localhost:8080 address

docker pull dpage/pgadmin4
docker run -p 80:80 -e "PGADMIN_DEFAULT_EMAIL=user@domain.com" -e "PGADMIN_DEFAULT_PASSWORD=SuperSecret" -d dpage/pgadmin4