(ns presentable
  (:require [presentable.core :as c]))

(defn log [x]
  (.log js/console x)
  x)

(defn obj->args [o]
  (apply concat (js->clj o :keywordize-keys true)))

(defn wrap [f]
  (fn [p]
    (f (clj->js p))))

(defn ^:export presenter [n o]
  (apply c/presenter (keyword n)
         (apply concat
                (update-in (js->clj o :keywordize-keys true)
                           [:factory]
                           wrap))))

(defn ^:export make
  ([n o]
     (apply c/make (keyword n) (obj->args o)))
  ([n]
     (make n (js-obj))))

(defn ^:export view [p]
  (c/view-of p))

(defn prop [p n]
  (clj->js (get p (keyword n))))
