ARG JAVA_VERSION=17.0.11_9-jre-jammy@sha256:c1cd13b3cc4e0ec634dec367b3769131947201352ca290f5da7d373f6a620393
ARG JAVA_IMAGE=eclipse-temurin

FROM ${JAVA_IMAGE}:${JAVA_VERSION}
ENV PORT 8080
ENV CLASSPATH /opt/lib
EXPOSE $PORT

ENV CONFIG_PROFILE dev
ENV LOGGING_CONFIG_FILE logback-spring.xml
ENV CONFIG_LOCATION /opt/application-${CONFIG_PROFILE}.yaml

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
ENV OTEL_JAVAAGENT_CONFIGURATION_FILE=/opt/otel/ot-default.properties

WORKDIR /opt
RUN mkdir ./otel

COPY target/*.jar app.jar
COPY target/classes/config/otel/*.properties ./otel

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED -noverify -XX:+AlwaysPreTouch -Duser.timezone=UTC -Djava.security.egd=file:/dev/./urandom -Dspring.cloud.config.enabled=false -Dspring.profiles.active=${CONFIG_PROFILE} -Dlogging.config=classpath:${LOGGING_CONFIG_FILE} -jar app.jar"]
