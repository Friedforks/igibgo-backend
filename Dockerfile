FROM azul/zulu-openjdk:21-latest

WORKDIR /app

COPY target/*.jar app.jar

RUN mkdir /tmpdir

EXPOSE 8001

ENTRYPOINT [ "java","-jar","/app/app.jar" ]