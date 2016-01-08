(ns identity.projection.user.read-model
  (:require [rill.message :as message]
            [rill.event-channel :as event-channel]
            [identity.domain.model.user :as user]))

(defn set-aggregate-version
  [model aggregate-id version]
  (assoc-in model [:aggregate-versions aggregate-id] version))

(defn aggregate-version
  [model aggregate-id]
  (get-in model [:aggregate-versions aggregate-id]))

(defn up-to-date?
  [model id version]
  (<= version (or (aggregate-version model id) -1)))


(defn by-id [model user-id]
  (get model user-id))

(defn full-name [first-name last-name]
  (str first-name " " last-name))

(defn add-user
  [model {:keys [tenant-id first-name last-name username email password]}]
  (let [user-id (user/user-id {:tenant-id tenant-id :username username})]
    (-> model
        (assoc-in [user-id :tenant-id] tenant-id)
        (assoc-in [user-id :first-name] first-name)
        (assoc-in [user-id :last-name] last-name)
        (assoc-in [user-id :username] username)
        (assoc-in [user-id :email] email)
        (assoc-in [user-id :password] password))))

(defn set-name
  [model user-id {:keys [first-name last-name]}]
  (-> model
      (assoc-in [user-id :first-name] first-name)
      (assoc-in [user-id :last-name] last-name)))

(defn set-password
  [model user-id password]
  (assoc-in model [user-id :password] password))

(defn set-enablement
  [model user-id enablement]
  (assoc-in model [user-id :enablement] enablement))

;; catchup

(defn caught-up
  [model]
  (assoc model :caught-up true))

(defn caught-up?
  [model]
  (boolean (:caught-up model)))
