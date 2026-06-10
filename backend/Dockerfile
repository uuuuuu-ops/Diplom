FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache fontconfig ttf-dejavu && fc-cache -fv
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx400m -XX:MaxMetaspaceSize=180m -XX:+UseSerialGC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
