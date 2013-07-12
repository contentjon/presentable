(ns presentable.core
  (:require [clojure.string :as str]
            [crate.core     :as crate]
            [jayq.core      :as jayq]))

(def application
  "Contains the application state at any given point in time"
  (atom {}))

(def current-id
  "Contains the a value that can be used to identify new
   presenter instances"
  (atom 0))

(defn- extend-app
  "Extends the application with new behavior/presenter types or
   new presenter instances"
  [type k obj]
  (swap! application assoc-in [type (get obj k)] obj))

(defn- next-id
  "Returns the next available presenter id"
  []
  (let [id @current-id]
    (swap! current-id inc)
    id))

(defn- fetch
  "Retrieves a value from the application state store"
  [type id]
  (get-in @application [type id]))

(defn property [id k]
  (get-in @application [:instances id k]))

(defn ->instance
  "Fetches the presenter instance behind id from the
   application state"
  [id]
  (fetch :instances id))

(defn ->id [id]
  (if (map? id) (:id id) id))

(def type-of
  "Returns the type of an instance"
  #(property (->id %) :type))

(def view-of
  "Returns the type of an instance"
  #(property (->id %) :view))

(defn- ->type [type instance]
  (map (partial fetch type) (get instance type)))

(defn- ->behaviors [instance]
  (->type :behaviors instance))

(defn- make-type
  "Creates a new meta type with a specific id and attached options"
  [id coll]
  (assoc (apply hash-map coll) :type id))

(defn- make-instance
  [prototype id coll]
  (let [args (apply hash-map coll)]
    (-> prototype
        (update-in [:behaviors] concat (:behaviors args))
        (merge (dissoc args :behaviors))
        (assoc :id id))))

(defn behavior
  "Creates a new behavior type in the application state"
  [id & rest]
  (extend-app :behaviors :type (make-type id rest)))

(defn presenter
  "Creates a new presenter type in the application state"
  [id & rest]
  (extend-app :presenters :type (make-type id rest)))

(defn- assemble
  "Calls the factory function of the instance and returns
   the result, which is the instances view"
  [instance]
  (let [factory (:factory instance)
        view    (factory instance)]
    (if (vector? view)
      (crate/html view)
      view)))

(defn- trigger-dom [dom evt args]
  (js/jQuery.prototype.trigger.apply
    (jayq/$ dom)
    (into-array (cons (name evt) args))))

(defn raise!
  "Raises an event on a presenter, which is propagated
   like a DOM event and can be picked ob by parent
   presenters. Extra parameter can be passed when raising,
   which will be visisble to every behaviour that gets
   called as a result"
  [id trigger & parameters]
  (let [instance (->instance (->id id))]
    (trigger-dom
      (:view instance)
      trigger
      parameters)))

(defn- dom->botid
  "Returns the presenter id of the nearest parent,
   that has such an id attached to it as an attribute"
  [dom]
  (let [$dom (jayq/$ dom)
        id   (loop [node $dom]
               (if-let [botid (jayq/attr node :presenter)]
                 botid
                 (recur (jayq/parent node))))]
    (int id)))

(defn trigger!
  "Directly trigger an event on a presenter. This event won't be
   propagated and will only result in reactions of the beaviours
   of this presenter"
  [id trigger & parameters]
  (let [instance (->instance id)]
    (doseq [behavior (->behaviors instance)]
      (when (contains? (set (:triggers behavior)) trigger)
        (when-let [reaction (:reaction behavior)]
          (apply reaction instance parameters))))))

(defn- changes->triggers [changes]
  (map #(keyword (str "update." (name %)))
       changes))

(defn- triggers-on? [behavior triggers]
  (some (set (:triggers behavior))
        triggers))

(defn- trigger-change!
  [id trigger changes]
  (let [instance (->instance id)
        triggers (changes->triggers changes)]
    (doseq [behavior (->behaviors instance)
            :when
            (triggers-on? behavior triggers)]
      (when-let [reaction (:reaction behavior)]
        (reaction instance changes)))))

(defn- target-type [evt]
  (-> (.-target evt)
      (dom->botid)
      (->instance)
      (:type)))

(defn- fill-in-type
  "If given an empty presenter type it will return
   whatever type the instance behind id was created from"
  [presenter id]
  (if (empty? presenter)
    (type-of id)
    (keyword presenter)))

(defn- event-handler
  "Returns a new event handler function, that triggers
   all behaviors applicable to this event"
  [trigger type id]
  (fn [evt & params]
    (when (= (target-type evt) (fill-in-type type id))
      (apply trigger! id trigger evt params))))

(defn- register-trigger
  "Registers a DOM event for the event type of a trigger"
  [trigger id view]
  (let [[type event] (str/split (name trigger) #"\.")]
    (jayq/on view event
      (event-handler trigger type id))))

(defn- register-triggers
  "Adds DOM events for all triggers of a presenter instance"
  [view instance]
  (doseq [trigger (:triggers instance)]
    (register-trigger trigger (:id instance) view)))

(defn make
  "Creates a new presenter instance in the application state
   and returns its identifier"
  [type & args]
  (when-let [prototype (fetch :presenters type)]
    (let [id       (next-id)
          instance (make-instance prototype id args)
          view     (assemble instance)
          result   (assoc instance :view view)
          ]
      (doto (jayq/$ view)
        (jayq/attr :presenter id)
        (register-triggers instance))
      (extend-app :instances :id result)
      (trigger! id :init nil)
      id)))

(defn update-behaviors [id f & args]
  (let [id (->id id)]
  	(swap! application update-in [:instances id :behaviors] f args)))

(defn add-behaviors! [id & behaviors]
  (update-behaviors id concat behaviors))

(defn rem-behaviors! [id & behaviors]
  (let [behaviors (set behaviors)]
    (update-behaviors id (partial remove behaviors))))

(defn ! [id & kvs]
  (let [id      (->id id)
        changes (apply hash-map kvs)]
    (swap! application
           update-in
           [:instances id]
           merge changes)
    (trigger-change! id :update (keys changes)))
  id)

(defn update! [id k f & args]
  (! id k (apply f (property (->id id) k) args)))
