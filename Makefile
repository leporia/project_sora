MAINCLASS := ChatClient

run: build
	cd bin && java -Dawt.useSystemAAFontSettings=on -cp ../lib/gson-2.6.2.jar:../lib/FiraMono.ttf:. $(MAINCLASS)

pack: build
	echo "Main-Class: $(MAINCLASS)" > jar/manifest.txt
	jar cvfm jar/$(MAINCLASS).jar jar/manifest.txt -C bin/ .
	rm jar/manifest.txt
	cp lib/gson-2.6.2.jar jar/
	cd jar && jar xf gson-2.6.2.jar
	cd jar && rm META-INF -R
	cd jar && jar xf ChatClient.jar
	cd jar && rm gson-2.6.2.jar
	cd jar && rm ChatClient.jar
	cp lib/FiraMono.ttf jar/
	cd jar && zip -r PSora.jar ./*
	cd jar && find . ! -name 'PSora.jar' -type f -exec rm -f {} +
	cd jar && rm -R com META-INF

build: dirs
	javac -cp lib/gson-2.6.2.jar:lib/FiraMono.ttf:. -d bin/ src/*.java

dirs:
	mkdir -p src bin jar

clean:
	rm -R jar/*
	rm bin/*
