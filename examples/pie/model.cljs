(ns presentable.examples.pie.model
  (:require [presentable.core :as ui]
            [tbn.events       :as evt]))

(ui/behavior :collection
  :triggers [:init]
  :reaction
  (fn [p]
    (doto (:collection p)
      (evt/on :added   #(ui/trigger! p :.added %))
      (evt/on :removed #(ui/raise! p :.removed %))
      (evt/on :reset   #(ui/raise! p :.reset (:collection p))))))

(ui/behavior :model
  :triggers [:init]
  :reaction
  (fn [p]
    (doto (:model p)
      (evt/on :changed #(ui/trigger! p :.changed %1 %2))
      (evt/on :error #(ui/raise! p :.error %1 %2)))))

(defn update-cmd [dom n]
  [:update-in [:data] :assoc n (jayq/val dom)])

(behavior :update-model
  :triggers [:.change]
  :reaction
  (fn [form evt]
    (let [dom (jayq/$ (.-target evt))
          n   (keyword (jayq/attr dom "name"))]
      (tbn/update! (:model form) (update-cmd dom n)))))
