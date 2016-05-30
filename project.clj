(defproject url-shortener "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [hiccup "1.0.5"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.postgresql/postgresql "9.3-1102-jdbc4"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler url-shortener.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
