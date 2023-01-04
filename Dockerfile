FROM clojure:temurin-11-tools-deps-1.11.1.1208-alpine AS base

FROM base AS jre
RUN $JAVA_HOME/bin/jlink \
    --add-modules java.base,java.logging,java.net.http,java.sql,java.sql.rowset,java.transaction.xa,java.management \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /javaruntime

FROM base AS build
WORKDIR /usr/src/app
COPY ["deps.edn", "./"]
RUN clojure -P
RUN clojure -A:depstar -P
COPY . .
RUN clojure -M:kondo
RUN clojure -M:test -d ./test
RUN clojure -T:build uber

FROM alpine:3.17 as prod
ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
RUN mkdir /opt/app
WORKDIR /opt/app
COPY --from=jre /javaruntime $JAVA_HOME
COPY --from=build /usr/src/app/target/vamtyc-uber.jar .
EXPOSE 3000
ENTRYPOINT exec java $JAVA_OPTS -jar ./vamtyc-uber.jar
