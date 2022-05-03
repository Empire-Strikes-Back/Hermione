(ns Hermione.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]

   [reitit.ring]
   [reitit.http]
   [reitit.coercion.spec]
   [reitit.http.coercion]
   [reitit.dev.pretty]
   [sieppari.async.core-async]
   [reitit.interceptor.sieppari]
   [reitit.http.interceptors.parameters]
   [reitit.http.interceptors.muuntaja]
   [reitit.http.interceptors.exception]
   [reitit.http.interceptors.multipart]
  ;; Uncomment to use
  ; [reitit.http.interceptors.dev :as dev]
  ; [reitit.http.spec :as spec]
  ; [spec-tools.spell :as spell]
   [aleph.http]
   [muuntaja.core]
   [sieppari.async.manifold]
   [manifold.deferred :as d]
   [ring.util.response]

   [Hermione.seed]
   [Hermione.salt])
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(def stateA (atom nil))

(defn reload
  []
  (require '[Hermione.main] :reload))

(def app
  (reitit.http/ring-handler
   (reitit.http/router
    [#_["/*" (reitit.ring/create-file-handler
              {:root "out/ui"
               :index-files ["index.html"]})]

     #_["/ui/*" (reitit.ring/create-file-handler
                 {:root "out/ui"
                  :index-files ["index.html"]})]

     #_["/ui/*" (ring/create-resource-handler)]

     ["/api"

      ["/upload"
       {:post {:parameters {:multipart {:file reitit.http.interceptors.multipart/temp-file-part}}
               :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                          {:status 200
                           :body {:name (:filename file)
                                  :size (:size file)}})}}]

      ["/download"
       {:get {:handler (fn [_]
                         {:status 200
                          :headers {"Content-Type" "image/png"}
                          :body (Wichita.java.io/input-stream
                                 (Wichita.java.io/resource "reitit.png"))})}}]


      ["/async"
       {:get {:handler (fn [{{{:keys [seed results]} :query} :parameters}]
                         (d/chain
                          (aleph.http/get
                           "https://randomuser.me/api/"
                           {:query-params {:seed seed, :results results}})
                          :body
                          (partial muuntaja.core/decode "application/json")
                          :results
                          (fn [results]
                            {:status 200
                             :body results})))}}]

      ["/Little-Rock"
       {:get {:handler (fn [{{{:keys []} :query} :parameters}]
                         (go
                           (<! (timeout 1000))
                           {:status 200
                            :body "twelve is the new twony"}))}}]

      ["/plus"
       {:get {:handler (fn [{{{:keys [x y]} :query} :parameters}]
                         {:status 200
                          :body {:total (+ x y)}})}
        :post {:handler (fn [{{{:keys [x y]} :body} :parameters}]
                          {:status 200
                           :body {:total (+ x y)}})}}]

      ["/minus"
       {:get {:handler (fn [{{{:keys [x y]} :query} :parameters}]
                         {:status 200
                          :body {:total (- x y)}})}
        :post {:handler (fn [{{{:keys [x y]} :body} :parameters}]
                          {:status 200
                           :body {:total (- x y)}})}}]]]


    {;:reitit.interceptor/transform dev/print-context-diffs ;; pretty context diffs
       ;;:validate spec/validate ;; enable spec validation for route data
       ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
     :conflicts nil
     :exception reitit.dev.pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :muuntaja muuntaja.core/instance
            :interceptors [;; query-params & form-params
                           (reitit.http.interceptors.parameters/parameters-interceptor)
                             ;; content-negotiation
                           (reitit.http.interceptors.muuntaja/format-negotiate-interceptor)
                             ;; encoding response body
                           (reitit.http.interceptors.muuntaja/format-response-interceptor)
                             ;; exception handling
                           (reitit.http.interceptors.exception/exception-interceptor)
                             ;; decoding request body
                           (reitit.http.interceptors.muuntaja/format-request-interceptor)
                             ;; coercing response bodys
                           (reitit.http.coercion/coerce-response-interceptor)
                             ;; coercing request parameters
                           (reitit.http.coercion/coerce-request-interceptor)
                             ;; multipart
                           (reitit.http.interceptors.multipart/multipart-interceptor)]}})
   (reitit.ring/routes
    (reitit.ring/create-resource-handler {:path "/"
                                          :root "ui"})
    (reitit.ring/create-default-handler))
   #_(reitit.ring/routes
      (fn
        ([request]
         (if (not (Wichita.string/starts-with? (:uri request) "/ui"))
           (ring.util.response/file-response "out/ui/index.html")
           request))
        ([request respond _]
         ([request]
          (if (not (Wichita.string/starts-with? (:uri request) "/ui"))
            (respond (ring.util.response/file-response "out/ui/index.html"))
            request))))
      (reitit.ring/create-default-handler))
   {:executor reitit.interceptor.sieppari/executor}))

(defn start []
  (let [port (or (try (Integer/parseInt (System/getenv "PORT"))
                      (catch Exception e nil))
                 3000)]
    (aleph.http/start-server (aleph.http/wrap-ring-async-handler #'app)
                             {:port port
                              :host "0.0.0.0"})
    (println (format "server running in port %s" port))))

(defn -main
  [& args]
  (reset! stateA {})
  (start))