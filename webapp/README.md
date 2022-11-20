# FlipFlapp

_Note: Running the application or the tests without Docker requires JDK 16._

## Run FlipFlapp without Docker

```console
# On Linux
$ ./mvnw clean spring-boot:run

# On Windows
$ .\mvnw clean spring-boot:run
```

## Run tests

```console
# On Linux
$ ./mvnw clean test

# On Windows
$ .\mvnw clean test
```

## Build Docker image

To build a new Docker image, run:

```bash
./mvnw clean spring-boot:build-image -Dspring-boot.build-image.imageName=docker.uibk.ac.at:443/informatik/qe/sepsss21/group3/g3t1/flipflapp:1.2.0 -Dmaven.test.skip=true
```

_Note: Depending on your Docker setup, you might need root permissions. Be aware that running the command as root will change the owner of files in `target` and `frontend/dist`. Just remove these directories afterwards._

## Push Docker image

_Note: Make sure you're logged in to docker.uibk.ac.at:443._

To push the newly built Docker image, run:

```bash
docker push docker.uibk.ac.at:443/informatik/qe/sepsss21/group3/g3t1/flipflapp:1.2.0
```

_Note: Depending on your setup, you might need root permissions._

## Test and Coverage reports

_Note: Requires JDK 16_

To generate Surefire and JaCoCo reports, run:

```sh
./mvnw clean surefire-report:report site -DgenerateReports=false jacoco:report
```

You can find the generated reports under `/target/site`:

* Surefire test report: `/target/site/surefire-report.html`
* JaCoCo coverage report: `/target/site/jacoco/index.html`

## Javadoc

To generate Javadoc, run:

```sh
./mvnw javadoc:javadoc
```

You can find the generated Javadoc under `/target/site/apidocs/index.html`.

## OpenAPI/Swagger specification

* JSON: <http://localhost:8080/v3/api-docs/>
* YAML: <http://localhost:8080/v3/api-docs.yaml>
* HTML: <http://localhost:8080/swagger-ui.html>