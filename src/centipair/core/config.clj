(ns centipair.core.config
  (:require [clojurewerkz.propertied.properties :as p]
            [clojure.java.io :as io]))


(defn load-db-config
  []
  (let [db-properties (p/load-from (io/file "config.properties"))
        db-map (p/properties->map db-properties true)]
    {:db (:db_name db-map)
     :user (:db_username db-map)
     :password (:db_password db-map)}))

(def db-config (load-db-config))
