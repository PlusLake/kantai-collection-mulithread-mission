.PHONY: main native

TARGET_PATH := ./target
SOURCE_PATH := ./src
JAVA_PATH := ${SOURCE_PATH}/java
RESOURCE_PATH := ${SOURCE_PATH}/resources
JAR_NAME := kancolle-multi.jar

main: clean compile package run

run:
	java -jar ./${TARGET_PATH}/${JAR_NAME}

compile:
	find ${JAVA_PATH} -type f -name "*.java" | xargs javac --source-path ${JAVA_PATH} -d ${TARGET_PATH}/classes

package:
	cp -r ./${RESOURCE_PATH}/* ./${TARGET_PATH}/classes
	jar cvf ./${TARGET_PATH}/${JAR_NAME} -C ./${TARGET_PATH}/classes .
	mkdir -p ./${TARGET_PATH}/archive && echo "Main-Class: main.Main" > ./${TARGET_PATH}/archive/manifest
	jar -uvfm ./${TARGET_PATH}/${JAR_NAME} ./${TARGET_PATH}/archive/manifest

native:
	# Work in progress; Input method still not usable with Liberica NIK
	# sudo apt install libxi-dev libxrender-dev
	# sdk install java 23.0.4.r17-nik
	native-image \
		-Djava.awt.headless=false \
		-H:IncludeResources="resource.*" \
		-jar kancolle-multi.jar native/kcmm

native-run:
	./native/kcmm

clean:
	test -d ${TARGET_PATH} && rm -r ${TARGET_PATH} || true
