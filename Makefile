.PHONY: main

main:
	find -name "*.java" -print | xargs javac -d target
	jar cvf kancolle-multi.jar -C target .
	echo "Main-Class: main.Main" > target/manifest
	jar -uvfm kancolle-multi.jar target/manifest
	java -jar kancolle-multi.jar

run:
	java -jar kancolle-multi.jar
