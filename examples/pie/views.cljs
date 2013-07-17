(ns presentable.examples.pie.views
  (:require [crate.core                  :as crate]
            [jayq.core                   :as jayq]
            [presentable.core            :as ui]
            [presentable.examples.pie.d3 :as d3]))

(defn children-in [presenter parent]
  (conj parent
        (map ui/view-of
             (:children presenter))))

(defn ink-grid [d]
  (children-in d [:div.ink-grid {:style "margin-top:20%"}]))

(defn ink-group [d]
  (children-in d [:div {:class "column-group gutters"}]))

(defn ink-column [d]
  (children-in d [:div {:class (str "large-" (:width d))}]))

(defn ink-button [d]
  (vector :button [(keyword (str "span." (name (:icon d))))]))

(defn editor []
  [:div
    (ui/view-of (ui/make :button :icon :icon-plus))
    [:div.models]])

(defn pie-form-field [[k v]]
  (let [n (name k)]
    [:div {:class "control-group large-33 medium-33 small-100"}
      [:div {:class "column-group quarter-gutters"}
        [:label {:for n :class "large-20 content-right"} n]
        [:div {:class "control large-80"}
          [:input {:type "text" :name n :value v}]]]]))

(defn ink-form-view [f data]
  [:form.ink-form
   (-> [:fieldset {:class "column-group quarter-gutters"}]
       (concat (map f data))
       (vec))])

(defn ink-pie [model]
  (ink-form-view pie-form-field model))

(defn update-forms [editor]
  (-> (d3/select (ui/view-of editor))
      (d3/select :.models)
      (d3/select* :form)
      (d3/data (into-array (:children editor)))
      (d3/entered)
      (d3/append ui/view-of)))

(defn pie [data]
  (let [layout (js/d3.layout.pie)]
    (layout (into-array data))))

(def arc
  (-> (js/d3.svg.arc)
      (.outerRadius 75)
      (.innerRadius 45)))

(def color
  (-> (d3.scale.ordinal)
      (.range
       (array "#98abc5"
              "#8a89a6"
              "#7b6888"
              "#6b486b"
              "#a05d56"
              "#d0743c"
              "#ff8c00"))))

(def root
  [:svg {:width 150 :height 150}
    [:g {:class "root"
         :transform (str "translate(" 75 "," 75 ")")}]])

(def pie-arc
  [:path {:class "arc" :d arc}])

(defn d3-pie-chart [model]
  (let [dom (crate/html [:div.pie])]
    (-> (d3/select dom)
        (d3/append root)
        (d3/select* :.arc)
        (d3/data (pie (vals model)))
        (d3/entered)
        (d3/append pie-arc)
        (d3/css :fill (fn [d i] (color i))))
    dom))

(defn update-pies [pies]
  (-> (d3/select (ui/view-of pies))
      (d3/select* :.pie)
      (d3/data (into-array (:children pies)))
      (d3/entered)
      (d3/append ui/view-of)))

(defn d3-update-pie [d _ model]
  (-> (d3/select (ui/view-of d))
      (d3/select :g)
      (d3/select* :.arc)
      (d3/data (pie (vals (:data model))))
      (d3/attr :d arc)))
