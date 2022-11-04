FROM amazoncorretto:17-alpine as corretto-jdk
LABEL org.opencontainers.image.source=https://github.com/navikt/omsorgspenger-proxy

RUN apk add --no-cache binutils
RUN apk add dumb-init

# Build small JRE image
RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

# main app image
FROM alpine:3.16
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV APP_JAR=/app/app.jar
ENV LC_ALL="nb_NO.UTF-8"
ENV LANG="nb_NO.UTF-8"
ENV TZ="Europe/Oslo"

RUN adduser --no-create-home -u 1000 -D someone
RUN mkdir /app && chown -R someone /app

COPY --from=corretto-jdk /customjre $JAVA_HOME
COPY --from=corretto-jdk /usr/bin/dumb-init /usr/bin/dumb-init
COPY /docker/init-scripts/ /app/init-scripts
COPY /docker/entrypoint.sh /app/entrypoint.sh
COPY /docker/run-java.sh /app/run-java.sh
COPY build/libs/app.jar /app/app.jar

RUN chown -R 1000:1000 /app/*

USER 1000
WORKDIR /app

ENTRYPOINT ["/usr/bin/dumb-init", "--", "/app/entrypoint.sh"]
