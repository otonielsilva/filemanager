# Simple File Manager

This project was built using Quarkus, MySQL and storing binary files in local folders. 

![Build GitHub](https://github.com/otonielsilva/filemanager/actions/workflows/maven.yml/badge.svg)

# Requirements
- Java 11+
- Maven 3.8+
- Docker
- Docker Compose

# Development Mode
This mode can be used as quick start and for evaluation purposes.
 - Execute MySQL container: `docker-compose up mysql-db`
 - Run `./mvnw quarkus:dev` for debug mode `./mvnw quarkus:dev -Ddebug`

# Sample of requests: 
This application uses the API First principles, you may import the file `openapi.yaml` (OpenAPI v3) into Postman or other tools.
You may also open Swagger UI (http://localhost:8080/q/swagger-ui/) in development mode.  

### Upload a new file
```
-------------- REQUEST  --------------
curl --location --request POST 'http://localhost:8080/files' \
--header 'Content-Type: multipart/form-data' \
--form 'file=@"/home/user/file.jpg"' \
--form 'fileName="file.jpg"'

-------------- RESPONSE  --------------
{
    "id": "df9b8e0f-c30f-4530-8677-6696f5d504b0",
    "originalFileName": "file.jpg",
    "lastVersion": 1,
    "size": 5464266,
    "checksum": "d4f9bfe4ad7914949776dacd2a5b309d336ae4e8d480df32c9c4c826f14e4c5f",
    "createdAt": "2021-11-16T23:02:45.775816",
    "updatedAt": "2021-11-16T23:02:45.775829"
}
```
The new upload will generate a new ID in the response which will be used in the next requests. In the example above, the ID is `df9b8e0f-c30f-4530-8677-6696f5d504b0`


## Get file metadata
```
-------------- REQUEST  --------------
curl --location --request GET 'http://localhost:8080/files/df9b8e0f-c30f-4530-8677-6696f5d504b0' 

-------------- RESPONSE  --------------
{
    "id": "df9b8e0f-c30f-4530-8677-6696f5d504b0",
    "originalFileName": "file.jpg",
    "lastVersion": 1,
    "size": 5464266,
    "checksum": "d4f9bfe4ad7914949776dacd2a5b309d336ae4e8d480df32c9c4c826f14e4c5f",
    "createdAt": "2021-11-16T23:02:45.775816",
    "updatedAt": "2021-11-16T23:02:45.775829"
}
```

## Upload another version of file
```
-------------- REQUEST  --------------
curl --location --request PUT 'http://localhost:8080/files/df9b8e0f-c30f-4530-8677-6696f5d504b0' \
--header 'Content-Type: multipart/form-data' \
--form 'file=@"/home/user/file2.jpg"' 
--form 'fileName="file2.jpg"'

-------------- RESPONSE  --------------
{
    "id": "fd2cdb87-84b4-4228-923b-eec505838669",
    "originalFileName": "file2.jpg",
    "lastVersion": 2,
    "size": 5464266,
    "checksum": "d4f9bfe4ad7914949776dacd2a5b309d336ae4e8d480df32c9c4c826f14e4c5f",
    "createdAt": "2021-11-16T23:16:10.63632",
    "updatedAt": "2021-11-16T23:16:10.636388"
}
```

## Download file
```
-------------- REQUEST  --------------
curl --location --request GET 'http://localhost:8080/files/df9b8e0f-c30f-4530-8677-6696f5d504b0/file?version=1' --output file.jpg 
```

## Delete file and all their versions
```
-------------- REQUEST  --------------
curl --location --request GET 'http://localhost:8080/files/df9b8e0f-c30f-4530-8677-6696f5d504b0
```

# Running the project 
- Execute a build: `./mvnw package`
- Build the container: `docker-compose build`
- Run project: `docker-compose up`
