(ns presentable.examples.menu
  "This example shows how to create a simple menu with
   presenters for menu items and a menu bar"
  (:require [clojure.string :as str]
            [jayq.core      :as jayq]
            [presentable.core :refer (behavior presenter) :as ui]))

(def colors
  [:red :orange :blue :green :grey :black])

(defn children-in [presenter parent]
  (conj parent
        (map ui/view-of (:children presenter))))

;; first we define some view function, which use the ink
;; css framework to generate a nice looking menu. these
;; functions are going to be used later, to instanciate
;; views and attach them to the presenters

(defn ink-menu-view [menu]
  [:nav.ink-navigation
    (children-in menu
      [:ul {:class "menu horizontal red rounded shadowed"}])])

(defn ink-submenu-view [menu]
  [:li [:a {:href "#"} (:label menu)]
   (children-in menu
     [:ul {:class "submenu shadowed"}])])

(defn ink-menu-item [item]
  [:li [:a {:href "#"} (:label item)]])

;;; the following presenters define a menu and its items

;; the menu presenter has little function expect to be a container
;; for menu items and sub menus. it simply passes it's content
;; to the view factory function

(presenter :menu
  :factory ink-menu-view)

;; the menu item presenter reacts to clicks and can be combined with
;; behaviors to react to those clicks. by default an item changes the
;; color of the menu when clicked

(presenter :item
  :label     "You forgot the label!"
  :factory   ink-menu-item)

;;; behaviors separate the functionality of the ui from the data.
;;; the can be attached to presenters dynamically. This makes it
;;; easy to change beahvior at runtime

;; items trigger a special activate event when they get clicked

(behavior :color-picked #(ui/raise! %1 :color-picked (:color %1)))

;; adding this behavior prevents the default behavior of links,
;; which would otherwise reload the page

(behavior :prevent-default #(.preventDefault %2))

;; just a cheap effect that changes the color class of an element
;; when an item gets activated

(behavior :change-color
  (fn [presenter evt color]
    (let [classes (str/join " " (map name colors))]
      (-> (jayq/$ :.menu (ui/view-of presenter))
          (jayq/remove-class classes)
          (jayq/add-class color)))))

;;; to make menu creation easier, we throw in some custom
;;; constructor functions

(defn menu [& children]
  (ui/make :menu
           :children (vec children)
           :on       {:item.color-picked [:change-color]}))

(defn item [label color action]
  (ui/make :item
           :label label
           :color color
           :on    {:.click [action]}))

;; a submenu uses the item presenter but changes its view so a
;; fold out menu is displayed instead of a clickable button

(defn submenu [label & children]
  (ui/make :item
    :factory  ink-submenu-view
    :label    label
    :children (vec children)))

(def the-menu
  (menu
    (apply submenu "Colors"
      (map #(item (name %) % :color-picked)
           colors))))

(defn ^:export init []
  (-> (jayq/$ :#content)
      (jayq/append (ui/view-of the-menu))))
