(ns Hermione.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string :as Wichita.string]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   [Hermione.drawing]
   [Hermione.seed]
   [Hermione.raspberries]
   [Hermione.salt]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce express (js/require "express"))

(defonce ^:const PORT 3000)
(def server (express))

(.use server (.static express "ui"))

(.get server "*" (fn [request response]
                   (.sendFile response (.join path js/__dirname  "ui" "index.html"))))

(.listen server 3000
         (fn []
           (js/console.log (format "server started on %s" PORT))))

(defn -main []
  (println "or worse - expelled")
  (println "i dont want my next job")
  (println "Kuiil has spoken"))