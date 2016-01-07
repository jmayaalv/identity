(ns identity.domain.model.user
  (:require [schema.core :as s]
            [clj-time.core :as time]
            [rill.message :refer [defevent primary-aggregate-id]]
            [rill.event-store :refer [retrieve-events]]
            [rill.aggregate :refer [handle-event load-aggregate]])
  (:import (java.util Date)))

(defrecord User [tenant-id first-name last-name username password enablement])
(defrecord ContactInformation [email])
(defrecord Enablement [enabled start-date])
(defrecord Address [country-code city postal-code province street])
(defrecord Authentication [user-id username])

(defn user-id
  [{:keys [tenant-id username]}]
  (str "user: " tenant-id ":" username))

(defn indefinite-enablement []
  (->Enablement true (time/now)))

(defn disable-enablement []
  (->Enablement false (time/now)))

(defn full-name [{:keys [first_name last_name]}]
  (str first_name " " last_name))

(defn encrypt!
  "Encrypts a string x with a salt s"
  [x s]
  x)

(defn enabled?
  "Determines if an User is enabled"
  [{:keys [enablement]}]
  (let [enabled (:enabled enablement)
        now (time/now)
        start-date (:start-date enablement)
        end-date (or (:end-date enablement) now)]
    (and enabled (time/within? start-date end-date now))))

(defn disabled? [tenant]
  ((complement enabled?) tenant))

(defn valid-credentials? [{:keys [tenant-id username password]} c_username c_password]
  (and (= username c_username)
       (= password (encrypt! c_password tenant-id))))

(defn invalid-credentials? [user username password]
  ((complement valid-credentials?) user username password))

(defevent Registered
          :tenant-id s/Uuid
          :first-name s/Str
          :last-name s/Str
          :email s/Str
          :username s/Str
          :password s/Str
          user-id)

(defmethod handle-event ::Registered
  [_ {:keys [tenant-id first-name last-name email username password]}]
  (let [user (->User tenant-id first-name last-name username password (disable-enablement))
        contact-info (->ContactInformation email)]
    (assoc user :contact-info contact-info)))

(defevent PasswordChanged
          :tenant-id s/Uuid
          :username s/Uuid
          :password s/Str
          user-id)

(defmethod handle-event ::PasswordChanged
  [user {:keys [password]}]
  (assoc user :password password))

(defevent NameChanged
          :tenant-id s/Uuid
          :username s/Str
          :first-name s/Str
          :last-name s/Str
          user-id)

(defmethod handle-event ::NameChanged
  [user {:keys [first-name last-name]}]
  (assoc user :first-name first-name
              :last-name last-name))

(defevent EnablementDefined
          :tenant-id s/Uuid
          :username s/Str
          :start-date Date
          :end-date Date
          user-id)

(defmethod handle-event ::EnablementDefined
  [user {:keys [start-date end-date]}]
  (-> user
      (assoc-in [:enablement :start-date] start-date)
      (assoc-in [:enablement :end-date] end-date)))

(defevent Enabled
          :tenant-id s/Uuid
          :username s/Str
          user-id)

(defmethod handle-event ::Enabled
  [user _]
  (-> user
      (assoc-in [:enablement :enabled] true)
      (assoc-in [:enablement :start-date] (time/now))
      (assoc-in [:enablement :end-date] nil)))

(defevent Disabled
          :tenant-id s/Uuid
          :username s/Str
          user-id)

(defmethod handle-event ::Disabled
  [user _]
  (-> user
      (assoc-in [:enablement :enabled] false)
      (assoc-in [:enablement :end-date] (time/now))))

;(defevent ContactInformationCreated
;          :user-id s/Uuid
;          :email s/Str
;          :primary-phone s/Str
;          :secondary-phone s/Str
;          :country-code s/Str
;          :city s/Str
;          :province s/Str
;          :postal-code s/Str
;          :street s/Str)
;
;(defevent AddressUpdated
;          :user-id s/Uuid
;          :country-code s/Str
;          :city s/Str
;          :province s/Str
;          :postal-code s/Str
;          :street s/Str)
;
;(defevent PrimaryPhoneUpdated
;          :user-id s/Uuid
;          :phone s/Str)
;
;(defevent SecondaryPhoneUpdated
;          :user-id s/Uuid
;          :phone s/Str)


;(defevent RoleAdded
;          :user-id s/Uuid
;          :role s/Keyword)

