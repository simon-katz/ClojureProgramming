(ns com.clojurebook.url-shortener
  (:require [compojure.core :refer [GET PUT POST defroutes]]
            [compojure.handler :refer [api]]
            [compojure.route :refer [not-found]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [redirect]]))

;;;; ___________________________________________________________________________
;;;; ---- Concrete state ----

(def ^:private counter (atom 0))

(def ^:private mappings (ref {}))

;;;; ___________________________________________________________________________
;;;; ---- Working with state ----

(defn url-for [id] (@mappings id))

(defn ids [] (keys @mappings))

(defn shorten!
 "Stores the given URL under a new unique identifier, or the given identifier
  if provided.  Returns the identifier as a string.
  Modifies the global mapping accordingly." 
 ([url]
  (let [id (swap! counter inc)
        id (Long/toString id 36)]
    (or (shorten! url id)
        (recur url))))
 ([url id]
   (dosync
     (when-not (@mappings id)
       (alter mappings assoc id url)
       id))))

;;;; ___________________________________________________________________________
;;;; ---- Web front end ----

(defn retain ; **** why no ! if shorten! has one?
  [& [url id :as args]]
  (if-let [id (apply shorten! args)]
    {:status 201
     :headers {"Location" id}
     :body (format "URL %s assigned the short identifier %s" url id)}
    {:status 409 :body (format "Short URL %s is already taken" id)}))

(defn redirect-for-id
  [id]
  (if-let [url (url-for id)]
    (redirect url)
    {:status 404 :body (str "No such short URL: " id)}))

(defroutes app*
  (GET "/" request "Welcome!")
  ;; **** this is for a PUT request where the URL is a single segment
  (PUT "/:id" [id url] (retain url id))
  ;; **** this is for any POST request
  (POST "/" [url] (if (empty? url)
                    {:status 400 :body "No `url` parameter provided"}
                    (retain url)))
  (GET "/:id" [id] (redirect-for-id id))
  (GET "/list/" [] (let [ids (ids)]
                     (if (seq ids)
                       (interpose "\n" ids)
                       "Nothing registered.")))
  (not-found "Sorry, there's nothing here."))

(def app (api app*))

;; ; To run locally:
#_
(def server (run-jetty #'app {:port 8080 :join? false}))
#_
(.stop server)

;;;; ___________________________________________________________________________
;;;; Why to use #' ...
;;;; Change definition of my-handler while the server is running.
;;;; If the server has the ref, that's cool.
;;;; If the server has the function (the result of evaluating 'my-handler'),
;;;; no good.

(defn my-handler
  [{:keys [uri]}]
  {:body (format "You requested %s" uri)})

;; (def server (run-jetty #'my-handler {:port 8080 :join? false}))
;; (def server (run-jetty my-handler {:port 8080 :join? false}))
;; (.stop server)

;;;; ___________________________________________________________________________
