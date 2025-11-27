FROM bitnami/java

WORKDIR /app
COPY . /app

RUN javac -d bin src/*
RUN javac -cp lib/*.jar -d out src/* test/*

WORKDIR /app/bin
