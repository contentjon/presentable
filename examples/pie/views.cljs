(ns presentable.examples.pie.views
  (:require [jayq.core                   :as jayq]
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

(defn d3-pie-chart [parent model]
  (let [svg (-> (d3/select (ui/view-of parent)) (d3/append :svg))]
    (-> svg
        (d3/attr :width 150)
        (d3/attr :height 150)
        (d3/append :g)
        (d3/attr :transform (str "translate(" 75 "," 75 ")"))
        (d3/select* :.arc)
        (d3/data (pie (vals model)))
        (d3/entered)
        (d3/append :path)
        (d3/attr :class "arc")
        (d3/attr :d arc)
        (d3/css :fill (fn [d i] (color i))))
    (.node svg)))

(defn d3-update-pie [d _ model]
  (-> (d3/select (ui/view-of d))
      (d3/select :g)
      (d3/select* :.arc)
      (d3/data (pie (vals (:data model))))
      (d3/attr :d arc)))
