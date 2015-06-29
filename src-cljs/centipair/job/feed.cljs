(ns centipair.job.feed
  (:require [reagent.core :as reagent]
            [centipair.core.ui :as ui]
            [centipair.core.utilities.ajax :as ajax]
            [centipair.core.utilities.spa :as spa]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))


(def side-menu-items (reagent/atom [{:label "All" :url "/category/all" :id "all" :active false}
                                    {:label "Web development" :url "/category/web-development" :id "web-development" :active false}
                                    {:label "Mobile development" :url "/category/mobile-development" :id "mobile-development" :active false}
                                    {:label "Content writing" :url "/category/content-writing" :id "content-writing" :active false}]))


(defn side-menu-item [item]
  [:a {:href (str "#" (:url item))
       :class "list-group-item"
       :key (str "side-menu-item-" (:id item))}
   (:label item)])


(defn side-menu []
  [:div {:class "list-group"}
   (doall (map side-menu-item @side-menu-items))])


(defn render-side-menu []
  (ui/render side-menu "side-menu"))



(defn search-bar
  []
  [:div {:class "input-group"}
   [:input {:type "text" :class "form-control" :placeholder "Search jobs"}
    [:span {:class "input-group-btn"}
     [:button {:class "btn btn-default" :type "button"}
      [:i {:class "fa fa-search"}]]]]])

(defn render-search-bar
  []
  (ui/render search-bar "search-bar"))


(def job-list-data (reagent/atom {}))

(defn clean-budget
  [])

(defn job-list-row
  [each-job]
  [:div {:class "row job-list"
         :key (str "job-list-row-key-" (:job_id each-job))
         :on-click #(js/alert "clicked")}
   [:div {:class "col-md-7"
          :key (str "job-list-column-1-key-" (:job_id each-job))}
    (:job_title each-job)]
   [:div {:class "col-md-2"
          :key (str "job-list-column-2-key-" (:job_id each-job))}
    (:job_location each-job)]
   [:div {:class "col-md-2"
          :key (str "job-list-column-3-key-" (:job_id each-job))}
    (:job_type each-job)]
   [:div {:class "col-md-1"
          :key (str "job-list-column-4-key-" (:job_id each-job))}
    (str "$"(:job_budget each-job))]])

(defn job-list
  []
  [:div
   (doall 
    (map job-list-row (:data @job-list-data)))])


(defn render-job-list
  []
  (ui/render job-list "job-list"))


(defn fetch-job-list
  [params]
  (ajax/get-json
   "/api/public/jobs"
   params
   (fn [response]
     (.log js/console response)
     (swap! job-list-data assoc :data (:result response)) )))



(defn render-home-components [query-params]
  (do 
    ;;(render-side-menu)
    (render-search-bar)
    (render-job-list)
    (fetch-job-list query-params)
    ))


(defroute applications "/jobs" [query-params]
  (js/console.log "jobs")
  (js/console.log (clj->js query-params))
  (render-home-components query-params))



(defn init-feed
  []
  (if (spa/home-page?)
    (spa/redirect "/jobs?page=0")))

(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))
