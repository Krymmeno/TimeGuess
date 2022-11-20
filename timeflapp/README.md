# TimeFlapp

What is not mentioned there is that we also have an image for amd64: docker.uibk.ac.at:443/informatik/qe/sepsss21/group3/g3t1/timeflapp:1.2.0-amd64

## Debugging

If you'd like to debug the application, run the container with `-e "JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"` and configure Remote JVM Debug in IntelliJ IDEA.

## Build image

To build a new image, run:

```sh
# On arm32v7 (Raspberry Pi):
sudo docker build -f Dockerfile.arm32v7 -t docker.uibk.ac.at:443/informatik/qe/sepsss21/group3/g3t1/timeflapp:1.2.0-arm32v7 .

# On amd64:
sudo docker build -f Dockerfile.amd64 -t docker.uibk.ac.at:443/informatik/qe/sepsss21/group3/g3t1/timeflapp:1.2.0-amd64 .
```

## Push image

_Note: Make sure you're logged in to docker.uibk.ac.at:443._

To push the newly built image, run:

```sh
# On arm32v7 (Raspberry Pi):
sudo docker push docker.uibk.ac.at:443/informatik/qe/sepsss21/group3/g3t1/timeflapp:1.2.0-arm32v7

# On amd64:
sudo docker push docker.uibk.ac.at:443/informatik/qe/sepsss21/group3/g3t1/timeflapp:1.2.0-amd64
```

## Test and Coverage reports

_Note: Requires JDK 1.8_

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
