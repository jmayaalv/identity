(ns identity.projection.user.event-handler
  (:require [rill.message :as message]
            [rill.event-channel :as event-channel]
            [clj-time.core :as time]
            [identity.domain.model.user :as user]
            [identity.projection.user.read-model :as model]))

(defmulti handle-event
          (fn [model event] (message/type event)))

(defmethod handle-event :default
  [m _]
  m)



(defmethod handle-event ::user/Registered
  [m {:keys [tenant-id first-name last-name username email password ::message/number]}]
  (-> m
      (model/add-user {:tenant-id  tenant-id
                       :first-name first-name
                       :last-name  last-name
                       :username   username
                       :email      email
                       :password   password})
      (model/set-aggregate-version (user/user-id {:tenant-id tenant-id :username username}) number)))

(defmethod handle-event ::user/NameChanged
  [m {:keys [tenant-id username first-name last-name ::message/number]}]
  (let [user-id (user/user-id {:tenant-id tenant-id :username username})]
    (-> m
        (model/set-name user-id {:first-name first-name :last-name  last-name})
        (model/set-aggregate-version user-id number))))

(defmethod handle-event ::user/PasswordChanged
  [m {:keys [tenant-id username password ::message/number]}]
  (let [user-id (user/user-id {:tenant-id tenant-id :username username})]
    (-> m
        (model/set-password user-id password)
        (model/set-aggregate-version user-id number))))

(defmethod handle-event ::user/Disabled
  [m {:keys [tenant-id username ::message/number]}]
  (let [user-id (user/user-id {:tenant-id tenant-id :username username})
        enablement (-> (:enablement m)
                       (assoc :enabled false)
                       (assoc :end-date (time/now)))]
    (-> m
        (model/set-enablement user-id enablement)
        (model/set-aggregate-version user-id number))))

(defmethod handle-event ::user/Enabled
  [m {:keys [tenant-id username ::message/number]}]
  (let [user-id (user/user-id {:tenant-id tenant-id :username  username})
        enablement (-> (:enablement m)
                       (assoc :enabled true)
                       (assoc :start-date (time/now))
                       (assoc :end-date nil))]
    (-> m
        (model/set-enablement user-id enablement)
        (model/set-aggregate-version user-id number))))

; ready for display

(defmethod handle-event ::event-channel/CaughtUp
  [model _]
  (model/caught-up model))