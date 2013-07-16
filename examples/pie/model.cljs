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
