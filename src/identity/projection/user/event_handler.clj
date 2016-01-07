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
  [m {:keys [tenant-id first-name last-name username email password]}]
  (model/add-user m {:tenant-id  tenant-id
                     :first-name first-name
                     :last-name  last-name
                     :username   username
                     :email      email
                     :password   password}))

(defmethod handle-event ::user/NameChanged
  [m {:keys [tenant-id username first-name last-name]}]
  (model/set-name m
                  (user/user-id {:tenant-id tenant-id
                                 :username  username})
                  {:first-name first-name
                             :last-name  last-name }))

(defmethod handle-event ::user/PasswordChanged
  [m {:keys [tenant-id username password]}]
  (model/set-password m
                      (user/user-id {:tenant-id tenant-id
                                       :username  username})
                      {:tenant-id tenant-id
                       :username  username
                       :password  password}))

(defmethod handle-event ::user/Disabled
  [m  {:keys [tenant-id username]}]
  (let [enablement (-> (:enablement  m)
                       (assoc :enabled false)
                       (assoc :end-date (time/now)))]
    (model/set-enablement m
                          (user/user-id {:tenant-id tenant-id
                                           :username  username})
                          enablement)))

(defmethod handle-event ::user/Enabled
  [m  {:keys [tenant-id username]}]
  (let [enablement (-> (:enablement m)
                       (assoc :enabled true)
                       (assoc :start-date (time/now))
                       (assoc :end-date nil))]
    (model/set-enablement m
                          (user/user-id {:tenant-id tenant-id
                                         :username  username})
                          enablement)))

; ready for display

(defmethod handle-event ::event-channel/CaughtUp
  [model _]
  (model/caught-up model))


