(defproject com.clojurebook/url-shortener "1.0.0-SNAPSHOT"
  :description "A toy URL shortener HTTP service written using Ring and Compojure."
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring "1.1.8"]]
  :plugins [[lein-ring "0.8.5"]
            ;;[lein-beanstalk "0.2.2"]
            ]
  :ring {:handler com.clojurebook.url-shortener.beanstalk/app})
