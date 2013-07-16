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

;; first we define a store, which is simply an atom for this example
;; then we create a collection to work on, which is a mutable list
;; of models

(def store      (mem/make))
(def collection (tbn/collection store :pies))

;; these are just some simple presenters that wrap the static bits
;; of the page.

(presenter :grid
  :factory view/ink-grid)

(presenter :group
  :factory view/ink-group)

(presenter :column
  :width    50
  :factory  view/ink-column)

;;; the following prsenters and behaviors define a simple model editor
;;; that is going to end up on the left hand side of the screen

(presenter :button
  :triggers [:.click]
  :icon     nil
  :factory  view/ink-button)

;; the editor component has a property that holds the collection we
;; created. the first behavior in the presenter binds the events of
;; the collection to triggers of the presenter. here we use the
;; "added" event of the collection, which gets fired every time a new
;; model is added to the collection

(presenter :editor
  :triggers   [:button.click :.added]
  :behaviors  [:collection :add-pie-model :edit-model]
  :collection collection
  :forms      []
  :factory    view/editor)

(behavior :add-pie-model
  :triggers [:button.click]
  :reaction #(tbn/conj! (:collection %) {:data {:a 1 :b 1 :c 1}}))

;; this behavior creates a new form when a model is added
;; the new form is set to edit the properties of the new model

(behavior :edit-model
  :triggers [:.added]
  :reaction
  (fn [editor model]
    (let [view (ui/view-of editor)
          form (ui/make :pie-form :model model)]
      (-> (jayq/$ :.models)
          (jayq/append (ui/view-of form)))
      (ui/update! editor :forms conj form))))

;; this presenter is bound to the events of a model through the :model
;; behavior. the task of the presenter is a two way binding between
;; a set of form properties and the model properties

(presenter :pie-form
  :triggers  [:.changed :.error :.change]
  :behaviors [:model :update-model]
  :factory   #(view/ink-pie (:data @(:model %))))

;;; the following presenters and behaviors display all models in a pie
;;; chart. the pie charts are dynamically updated, when the model data
;;; changes

;; the pies presenter simply creates a root view for the pie charts.
;; it listens to the collection as well and creates new pie charts as
;; models get added to the collection

(presenter :pies
  :triggers   [:.added]
  :behaviors  [:collection :add-pie]
  :collection collection
  :factory    #(vector :div.pies))

(behavior :add-pie
  :triggers [:.added]
  :reaction #(ui/update! %1 :children conj (ui/make :pie :parent %1 :model %2)))

;; this presenter wraps a single pie chart and is bound to a single
;; model. it creates a binding between the layout of the pie chart and
;; the model. in this case the binding is one way as the chart can not
;; be edited directly

(presenter :pie
  :triggers  [:.changed]
  :behaviors [:model :update-pie]
  :factory   #(view/d3-pie-chart (:parent %) (:data @(:model %))))

(behavior :update-pie
  :triggers [:.changed]
  :reaction view/d3-update-pie)

;; finally initialize the app and show it to the user

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
