TARGET = minbuild-predeploy
SEMANTIC_DIR = ./resources/public/semantic
PROD_JAR = ./target/public/cljs-out/prod-main.js
EXAMPLE_DIR = ./docs/example

.PHONY:	clean run gh

all : $(TARGET)

package.lock: ./package.json
	npm i

clean:
	rm -rf ./target/*

$(SEMANTIC_DIR): package.lock ./semantic.json semantic/**/*
	cd ./semantic; npx gulp build; cd ..

$(PROD_JAR): package.lock ./deps.edn ./prod.cljs.edn ./src/**/*
	clj -A\:fig\:min

$(EXAMPLE_DIR): $(SEMANTIC_DIR) $(PROD_JAR) ./resources/public/index.css ./resources/public/index.html
	mkdir -p docs/example
	cp -R resources/public/* docs/example/
	cp target/public/cljs-out/prod-main.js docs/example/index.js
	sed -i -e "s/cljs\-out\/dev\-main\.js/.\/index.js/" docs/example/index.html

gh: $(TARGET)
	git push -n origin HEAD

run: $(SEMANTIC_DIR)
	clj -A\:fig\:build

$(TARGET): clean $(EXAMPLE_DIR)
