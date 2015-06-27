(ns centipair.feed.home
  (:require [reagent.core :as reagent]
            [centipair.core.ui :as ui]))


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



(defn search-bar []
  [:div {:class "input-group"}
   [:input {:type "text" :class "form-control" :placeholder "Search projects"}
    [:span {:class "input-group-btn"}
     [:button {:class "btn btn-default" :type "button"}
      [:i {:class "fa fa-search"}]]]]])



(defn render-search-bar []
  (ui/render search-bar "search-bar"))


(defn render-components []
  (do 
    (render-side-menu)
    (render-search-bar)
    ))
