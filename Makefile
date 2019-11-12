TARGET = minbuild-predeploy

.PHONY:	clean semantic compile predeploy gh

all : $(TARGET)

npxi:
	npm i

clean:
	rm -rf ./target/public/cljs-out/prod*

semantic:
	cd ./semantic; npx gulp build; cd ..

compile:
	npx webpack
	clj -A:fig:min

example:
	mkdir -p docs/example
	cp -R resources/public/* docs/example/
	cp target/public/cljs-out/prod-main.js docs/example/index.js
	sed -i -e "s/cljs\-out\/dev\-main\.js/.\/index.js/" docs/example/index.html

gh: $(TARGET)
	git push -n origin HEAD

$(TARGET) : clean semantic compile example
