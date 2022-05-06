#!/bin/bash

repl(){
  clj \
    -J-Dclojure.core.async.pool-size=1 \
    -X:repl Ripley.core/process \
    :main-ns Hermione.main
}


main(){
  clojure \
    -J-Dclojure.core.async.pool-size=1 \
    -M -m Hermione.main
}

ui(){
  # watch release
  npm i --no-package-lock
  mkdir -p out/resources/ui/
  cp src/Hermione/index.html out/resources/ui/index.html
  clj -A:ui -M -m shadow.cljs.devtools.cli $1 ui
}

tag(){
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  git tag "$COMMIT_COUNT-$COMMIT_HASH" $COMMIT_HASH 
}

jar(){

  rm -rf out/*.jar
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  clojure \
    -X:uberjar Genie.core/process \
    :main-ns Hermione.main \
    :filename "\"out/Hermione-$COMMIT_COUNT-$COMMIT_HASH.jar\"" \
    :paths '["src" "out/resources"]'
}

release(){
  jar
}


"$@"