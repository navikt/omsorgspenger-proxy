FROM cgr.dev/chainguard/jre:openjdk17
LABEL org.opencontainers.image.source=https://github.com/navikt/omsorgspenger-proxy

ENV JDK_JAVA_OPTIONS="-Dhttp.proxyHost=webproxy.nais -Dhttps.proxyHost=webproxy.nais -Dhttp.proxyPort=8088 -Dhttps.proxyPort=8088 -Dhttp.nonProxyHosts=localhost|127.0.0.1|10.254.0.1|*.local|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no|*.nais.io|*.aivencloud.com|*.intern.dev.nav.no"
COPY build/libs/app.jar app.jar
CMD [ "-jar", "app.jar" ]
