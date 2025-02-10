# Stage 1 [graalvm]: compile native build stage
FROM ghcr.io/graalvm/native-image-community:21-ol9 AS native-build
WORKDIR /home/app
RUN microdnf install findutils
COPY . .
RUN ./gradlew --no-daemon nativeCompile

# Stage 2: use minified image to run app from native-build stage
FROM cgr.dev/chainguard/wolfi-base:latest
WORKDIR /app
COPY --from=native-build /home/app/build/native/nativeCompile/pokedex application
EXPOSE 8080
ENTRYPOINT ["/app/application"]