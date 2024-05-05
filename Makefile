.PHONY: main native

main: compile package run

run:
	java -jar kancolle-multi.jar

compile:
	find -name "*.java" | xargs javac -d target

package:
	cp -r resource/ target
	jar cvf kancolle-multi.jar -C target .
	echo "Main-Class: main.Main" > target/manifest
	jar -uvfm kancolle-multi.jar target/manifest

native:
	# Work in progress; Input method still not usable with Liberica NIK
	/opt/bellsoft/liberica-vm-24.0.1-openjdk22/bin/native-image \
		-Djava.awt.headless=false \
		-H:IncludeResources="resource.*" \
		-jar kancolle-multi.jar native/kcmm

native-run:
	./native/kcmm
