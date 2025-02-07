# Pokedex

## Prerequisites

The main requirements is the JDK. You may want use [**SDKMAN**](https://sdkman.io/) to easily handle all the JDK version on your local computer.

## How to run

### 1. Install SDKMAN

If you don't have SDKMAN installed, you can do so by running the following command in the terminal:

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

Since this project comes with a Gradle Wrapper you are not required to install Gradle. To run using Gradle just run (in the root of the project):

```bash
./gradlew run
```