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

jar(){

  rm -rf out/*.jar
  clojure \
    -X:uberjar Genie.core/process \
    :main-ns Hermione.main \
    :filename "\"out/Hermione-$(git rev-parse --short HEAD).jar\"" \
    :paths '["src" "out/resources"]'
}

ui(){
  # watch release
  npm i --no-package-lock
  mkdir -p out/resources/ui/
  cp src/Hermione/index.html out/resources/ui/index.html
  clj -A:ui -M -m shadow.cljs.devtools.cli $1 ui
}

"$@"