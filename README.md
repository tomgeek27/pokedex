# Pokedex

This is a [Java Micronaut](https://micronaut.io/) project.

## Prerequisites

The main requirements is the JDK (in this project the JDK 21 will be used). You may want use [**SDKMAN**](https://sdkman.io/) to easily handle all the JDK version on your local computer.

## How to run

### 1. Install SDKMAN

If you don't have SDKMAN installed, you can install it by running the following command in the terminal:

```bash
curl -s "https://get.sdkman.io" | bash
```

After installation, restart your terminal or run:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

### 2. Install JDK via SDKMAN

To run this project, you need to install JDK 21:

```bash
sdk install java 21.0.5-amzn 
```

And then, to be sure that no other version will create any conficts, set this as default in this way:

```bash
sdk default java 21.0.5-amzn
```

### 3. Run the project

Since this project comes with a Gradle Wrapper you are not required to install Gradle. To run a jar application using Gradle just run (in the root of the project):

```bash
./gradlew run
```

This will start the server application on the default port 8080.

## API Documentation

The API specification can be auto-generated using the command:

```bash
./gradlew compileJava
```

and then you will be available at: `build/classes/java/main/META-INF/swagger/pokedex-1.0.0.yml`. You can use tools like Swagger UI to visualize it.

## What could be different for a Production environment

### Architectural and Performance Enhancements

First of all understanding better the requisites could help to manage in a proper way the structure of the project in several aspects:
* **Adopt a Reactive Approach** – If this application is intended to serve a lot of request per seconds maybe could be useful adopting a reactive programming model instead of a blocking model. In particular, at the moment, every request is executed on the main thread so when a request is made (for instance an HTTP request to [pokeapi.co](https://pokeapi.co) or [funtranslations.com](https://funtranslations.com)) the process will wait for the response instead of serving other incoming requests. Thus, adopting a reactive programming would improve performance and resource efficiency, especially under high load.
* **Implement API Versioning** - Version the API could be useful (for instance `v1` or `v2` as prefix of the path) to guarantee the retro compatibility of the project and avoid possible breaking changes for the consumers.
* **Introduce Caching** - Adding a cache layer could help to improve performance, especially in a use-case like this where the response of pokeapi is pretty the same for each request. The cache layer could be implemented using a distributed key-value store (like redis) in a distributed system or simply using micronaut cache to store the response based on the request.
* **Monitoring** - Wrap the deployed instance with a monitoring system. For instance could be useful to wrap the deployed docker image with an APM (Application Performance Monitoring) agent of Elastic to trace all the incoming and outgoing requests through a specific flow, monitor the resources, the performances, any problem or exception that could raise and logs.

### Development and Deployment Best Practices

* **Git Hooks** – Implementing automated hooks to enforce test execution before any code is pushed to guarantee an higher code quality and reduced regression problems.
* **Application profiles** - Define several application profiles (`application-dev.yml` for instance) that reflect the deployed configuration leveraging on environment variables to manage profiles dynamically, ensuring flexibility and security. Use a secret management tool such as Infisical or HashiCorp Vault to securely inject sensitive configuration values, preventing hardcoded credentials and enhancing overall security.
* **Pipeline** - Adopting a CI/CD pattern enables the automation of integration and deployment processes, improving reliability and avoiding tedious and error-prone manual operations. A pipeline could include running tests, building a properly versioned Docker image, publishing it to a Docker registry, and finally deploying it in a containerized environment.
