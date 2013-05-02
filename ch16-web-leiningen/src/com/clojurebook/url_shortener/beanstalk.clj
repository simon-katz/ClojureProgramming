(ns com.clojurebook.url-shortener.beanstalk
  (:require [com.clojurebook.url-shortener :as the-main-app]
            [compojure.core :refer [HEAD defroutes]]))

(defroutes app
  ; This HEAD route is here because Amazon's Elastic Beanstalk determines if
  ; your application is up by whether it responds successfully to a
  ; HEAD request at /
  (HEAD "/" [] "")
  the-main-app/app)
