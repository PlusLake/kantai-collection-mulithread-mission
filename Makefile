.PHONY: main native

main: clean compile package run

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
	# sudo apt install libxi-dev libxrender-dev
	# sdk install java 23.0.4.r17-nik
	native-image \
		-Djava.awt.headless=false \
		-H:IncludeResources="resource.*" \
		-jar kancolle-multi.jar native/kcmm

native-run:
	./native/kcmm

clean:
	rm -r target
