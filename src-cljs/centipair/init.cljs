(ns centipair.init
  (:require [centipair.core.components.notifier :as notifier]
            [centipair.core.user.forms :as user-forms]
            [centipair.core.csrf :as csrf]
            [centipair.job.feed :refer [init-feed]]))



(defn ^:export init! []
  (do
    (notifier/render-notifier-component)
    (csrf/fetch-csrf-token)
    (init-feed)))
