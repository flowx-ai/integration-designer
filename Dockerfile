ARG JAVA_VERSION=17.0.10_7-jre-jammy@sha256:2da160772ec16d9d6a0c71585cf87b689dbbda531dc002de1856d8970cd0daf3
ARG JAVA_IMAGE=eclipse-temurin

FROM ${JAVA_IMAGE}:${JAVA_VERSION}
ENV PORT 8080
ENV CLASSPATH /opt/lib
EXPOSE $PORT

ENV CONFIG_PROFILE dev
ENV LOGGING_CONFIG_FILE logback-spring.xml
ENV CONFIG_LOCATION /opt/application-${CONFIG_PROFILE}.yaml

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

WORKDIR /opt

COPY target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED -noverify -XX:+AlwaysPreTouch -Duser.timezone=UTC -Djava.security.egd=file:/dev/./urandom -Dspring.cloud.config.enabled=false -Dspring.profiles.active=${CONFIG_PROFILE} -Dlogging.config=classpath:${LOGGING_CONFIG_FILE} -jar app.jar"]
