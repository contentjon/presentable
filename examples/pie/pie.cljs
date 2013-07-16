(ns presentable.examples.pie
  "In this example we show how to use the library to
   combine a model with the UI"
  (:require [clojure.string :as str]
            [jayq.core      :as jayq]
            [presentable.core :refer (behavior presenter) :as ui]
            [presentable.examples.pie.d3 :as d3]
            [tbn.core         :as tbn]
            [tbn.events       :as evt]
            [tbn.store.memory :as mem]))

(def store      (mem/make))
(def collection (tbn/collection store :pies))

(defn children-in [presenter parent]
  (conj parent
        (map ui/view-of
             (:children presenter))))

(presenter :grid
  :factory #(children-in % [:div.ink-grid {:style "margin-top:20%"}]))

(presenter :group
  :factory #(children-in % [:div {:class "column-group gutters"}]))

(presenter :column
  :width    50
  :factory  #(children-in % [:div {:class (str "large-" (:width %))}]))

(presenter :button
  :triggers [:.click]
  :icon     nil
  :factory  #(vector :button [(keyword (str "span." (name (:icon %))))]))

(presenter :editor
  :triggers   [:button.click :.added]
  :behaviors  [:collection :add-pie-model :edit-model]
  :collection collection
  :forms      []
  :factory
  (fn [this]
    [:div
      (ui/view-of (ui/make :button :icon :icon-plus))
      [:div.models]]))

(behavior :collection
  :triggers [:init]
  :reaction
  (fn [p]
    (doto (:collection p)
      (evt/on :added   #(ui/trigger! p :.added %))
      (evt/on :removed #(ui/raise! p :.removed %))
      (evt/on :reset   #(ui/raise! p :.reset (:collection p))))))

(behavior :model
  :triggers [:init]
  :reaction
  (fn [p]
    (doto (:model p)
      (evt/on :changed #(ui/trigger! p :.changed %1 %2))
      (evt/on :error #(ui/raise! p :.error %1 %2)))))

(behavior :add-pie-model
  :triggers [:button.click]
  :reaction #(tbn/conj! (:collection %) {:data {:a 1 :b 1 :c 1}}))

(behavior :edit-model
  :triggers [:.added]
  :reaction
  (fn [editor model]
    (let [view (ui/view-of editor)
          form (ui/make :pie-form :model model)]
      (-> (jayq/$ :.models)
          (jayq/append (ui/view-of form)))
      (ui/update! editor :forms conj form))))

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

(presenter :pie-form
  :triggers  [:.changed :.error :.change]
  :behaviors [:model :update-model]
  :factory   #(ink-form-view pie-form-field (:data @(:model %))))

(behavior :update-model
  :triggers [:.change]
  :reaction
  (fn [form evt]
    (let [dom (jayq/$ (.-target evt))
          n   (keyword (jayq/attr dom "name"))]
      (tbn/update! (:model form) [:update-in [:data] :assoc n (jayq/attr dom :value)]))))

(presenter :pies
  :triggers   [:.added]
  :behaviors  [:collection :add-pie]
  :collection collection
  :factory    #(vector :div.pies))

(behavior :add-pie
  :triggers [:.added]
  :reaction #(ui/update! %1 :children conj (ui/make :pie :parent %1 :model %2)))

(defn pie [data]
  (let [layout (js/d3.layout.pie)]
    (layout (into-array data))))

(def arc
  (-> (js/d3.svg.arc)
      (.outerRadius 75)
      (.innerRadius 45)))

(presenter :pie
  :triggers  [:.changed]
  :behaviors [:model :update-pie]
  :factory
  (fn [this]
    (-> (d3/select (ui/view-of (:parent this)))
        (d3/append :svg)
        (d3/attr :width 150)
        (d3/attr :height 150)
        (d3/append :g)
        (d3/attr :transform (str "translate(" 75 "," 75 ")"))
        (d3/select* :.arc)
        (d3/data (pie (vals (:data @(:model this)))))
        (d3/entered)
        (d3/append :path)
        (d3/attr :class :arc)
        (d3/attr :d arc))))

(def the-app
  (ui/make :grid
    :children
    [(ui/make :group
       :children
       [(ui/make :column :children [(ui/make :editor)])
        (ui/make :column :children [(ui/make :pies)])])]))

(defn ^:export init []
  (-> (jayq/$ :#content)
      (jayq/append (ui/view-of the-app))))
