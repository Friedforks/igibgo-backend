FROM maven:3.9.5-eclipse-temurin-21 as build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM azul/zulu-openjdk:21-latest

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir /tmpdir

EXPOSE 8001

ENTRYPOINT [ "java","-jar","/app/app.jar" ]