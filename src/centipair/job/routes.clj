(ns centipair.job.routes
  (:require [centipair.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [centipair.core.contrib.response :as response]))


(defn projects-dashboard []
  (layout/render
    "sbadmin.html"))


(defroutes dashboard-routes 
  (GET "/dashboard" [] (projects-dashboard)))
