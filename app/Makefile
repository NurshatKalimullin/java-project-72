.DEFAULT_GOAL := build-run

setup:
	./gradlew wrapper --gradle-version 7.4

clean:
	./gradlew clean

build:
	./gradlew clean build

install:
	./gradlew clean install

start-dist:
	APP_ENV=production ./build/install/java-project-72/bin/java-project-72

start:
	APP_ENV=development ./gradlew run

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest

generate-migrations:
	gradle generateMigrations

.PHONY: build