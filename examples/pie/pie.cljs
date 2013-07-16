(ns presentable.examples.pie
  "In this example we show how to use the library to
   combine a model with the UI"
  (:require [clojure.string :as str]
            [jayq.core      :as jayq]
            [presentable.core :refer (behavior presenter) :as ui]
            [presentable.examples.pie.model :as model]
            [presentable.examples.pie.views :as view]
            [tbn.core         :as tbn]
            [tbn.events       :as evt]
            [tbn.store.memory :as mem]))

(def store      (mem/make))
(def collection (tbn/collection store :pies))

(presenter :grid
  :factory view/ink-grid)

(presenter :group
  :factory view/ink-group)

(presenter :column
  :width    50
  :factory  view/ink-column)

(presenter :button
  :triggers [:.click]
  :icon     nil
  :factory  view/ink-button)

(presenter :editor
  :triggers   [:button.click :.added]
  :behaviors  [:collection :add-pie-model :edit-model]
  :collection collection
  :forms      []
  :factory    view/editor)

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

(presenter :pie-form
  :triggers  [:.changed :.error :.change]
  :behaviors [:model :update-model]
  :factory   #(view/ink-pie (:data @(:model %))))

(behavior :update-model
  :triggers [:.change]
  :reaction
  (fn [form evt]
    (let [dom (jayq/$ (.-target evt))
          n   (keyword (jayq/attr dom "name"))]
      (tbn/update! (:model form) [:update-in [:data] :assoc n (jayq/val dom)]))))

(presenter :pies
  :triggers   [:.added]
  :behaviors  [:collection :add-pie]
  :collection collection
  :factory    #(vector :div.pies))

(behavior :add-pie
  :triggers [:.added]
  :reaction #(ui/update! %1 :children conj (ui/make :pie :parent %1 :model %2)))

(presenter :pie
  :triggers  [:.changed]
  :behaviors [:model :update-pie]
  :factory   #(view/d3-pie-chart (:parent %) (:data @(:model %))))

(behavior :update-pie
  :triggers [:.changed]
  :reaction view/d3-update-pie)

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
