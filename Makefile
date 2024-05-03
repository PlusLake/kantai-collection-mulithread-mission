.PHONY: main

main: compile package run

run:
	java -jar kancolle-multi.jar

compile:
	find -name "*.java" -print | xargs javac -d target

package:
	cp -r resource/ target
	jar cvf kancolle-multi.jar -C target .
	echo "Main-Class: main.Main" > target/manifest
	jar -uvfm kancolle-multi.jar target/manifest
