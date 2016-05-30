(ns url-shortener.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]
            [ring.adapter.jetty :refer :all]
            [clojure.java.jdbc :as sql]
            [hiccup.core :as hiccup]))

(def ^:constant db {:subprotocol "postgresql"
                    :subname "//localhost:5432/fcc_provision"
                    :user "vagrant"
                    :password "foobar"})

(defn rand-base-36
  "Generates a random base 36 string, padded with 0s to 6 digits."
  []
  (clojure.string/replace
   ;; 36rZZZZZZ is the biggest 6-digit base 36 number.
   (format
    ;; Pad it with 6 spaces.
    "%6s"
    (Integer/toString (Math/floor (rand (inc 36rZZZZZZ))) 36))
   ;; There's no way to pad 0s with strings,
   ;; so just pad with spaces and replace with 0s.
   #" "
   "0"))

(def main-page
  (hiccup/html
   [:html
    [:body
     [:h1 "Type a URL here to create a shortened version."]
     [:form {:method "post", :action (str "/" (rand-base-36))}
      [:input {:type "text", :name "url", :placeholder "Input a URL here."}]
      [:input {:type "submit", :value "Submit"}]]]]))
      
(defn show-data [id]
  (let* [data (sql/query db ["SELECT url FROM pages WHERE id = ?" id])
        url-str (java.net.URLDecoder/decode (:url (first data)))]
    (hiccup/html
     [:html
      [:body
       [:p
        "This ID corresponds to the url: " url-str]
       [:p "To access it, go to: " url-str "/go"]]])))

(defn redirect-to
  "Redirects to the URL that corresponds to a certain ID in the database."
  [id]
  (let [data (sql/query db ["SELECT url FROM pages WHERE id = ?" id])]
    (redirect (java.net.URLDecoder/decode (:url (first data))))))

(defn url-from 
  "Accepts a Jetty HttpInput object and returns the URL parameter."
  [http-input]
  ;; Get the url out of the hash map.
  (get
   ;; Convert the vector into a hash map.
   (apply
    hash-map
    ;; Split on equals signs or ampersands.
    ;; This produces a vector where odd-indexed strings are keys,
    ;; and even-indexed strings are values.
    (clojure.string/split
     (slurp http-input)
     #"=|&"))
   "url"))

(defn make-new
  "Adds a new entry to the database."
  [id url]
  (sql/insert! db :pages {:id id, :url url})
  (redirect (str "/" id "/info")))

(defroutes app-routes
  ;; Where users create new pages.
  (GET "/" [] main-page)
  ;; Where users see info about each page (where it redirects to).
  (GET ["/:id{[a-z0-9]{6}}/info"] [id] (show-data id))
  ;; Redirects to the URL that corresponds to that ID.
  (GET  ["/:id{[a-z0-9]{6}}/go"] [id] (redirect-to id))
  ;; Creates new pages.
  (POST ["/:id{[a-z0-9]{6}}"] {{id :id} :params, body :body} (make-new id (url-from body)))
  (route/not-found "Not Found"))

;; Run the server.
(run-jetty app-routes {:port 3000})
