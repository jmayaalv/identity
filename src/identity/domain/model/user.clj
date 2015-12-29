(ns identity.domain.model.user
  (:require [schema.core :as s]
            [clj-time.core :as time]
            [rill.message :refer [defevent]]
            [rill.aggregate :refer [handle-event]])
  (:import (java.util Date)))

(defrecord User [user-id tenant-id first_name last_name username password enablement])
(defrecord ContactInformation [email])
(defrecord Enablement [start-date])
(defrecord Address [country-code city postal-code province street])
(defrecord Authentication [user-id username])

(defn indefinite-enablement []
  (->Enablement (time/now)))


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
  (complement enabled?) tenant)

(defn valid-credentials? [{:keys [user-id username password]} c_username c_password]
  (= (username c_username) (password (encrypt! c_password user-id))))

(defn invalid-credentials? [user username password]
  (complement valid-credentials?) user username password)

(defevent Registered
          :user-id s/Uuid
          :tenant-id s/Uuid
          :first-name s/Str
          :last-name s/Str
          :email s/Str
          :username s/Str
          :password s/Str)

(defmethod handle-event ::Registered
  [_ {:keys [user-id tenant-id first-name last-name email username password]}]
  (let [user (->User user-id tenant-id first-name last-name username password (indefinite-enablement))
        contact-info (->ContactInformation email)]
    (assoc user :contact-info contact-info)))

;(defevent Authenticated
;          :user-id s/Uuid)

(defevent EmailChanged
          :user-id s/Uuid
          :email s/Str)

(defmethod handle-event ::EmailChanged
  [user {:keys [email]}]
  (assoc-in user [:contact-info :email] email))

(defevent PasswordChanged
          :user-id s/Uuid
          :password s/Str)

(defmethod handle-event ::PasswordChanged
  [user {:keys [password]}]
  (assoc user :password password))

(defevent NameChanged
          :user-id s/Uuid
          :first-name s/Str
          :last-name s/Str)

(defmethod handle-event ::NameChanged
  [user {:keys [first-name last-name]}]
  (assoc user :first-name first-name
              :last-name last-name))

(defevent EnablementDefined
          :user-id s/Uuid
          :start-date Date
          :end-date Date)

(defmethod handle-event ::EnablementDefined
  [user {:keys [start-date end-date]}]
  (-> user
      (assoc-in [:enablement :start-date] start-date)
      (assoc-in [:enablement :end-date] end-date)))

(defevent Enabled
          :user-id s/Uuid)

(defmethod handle-event ::Enabled
  [user _]
  (-> user
      (assoc-in [:enablement :start-date] (time/now))
      (assoc-in [:enablement :end-date] nil)))

(defevent Disabled
          :user-id s/Uuid)

(defmethod handle-event ::Disabled
  [user _]
  (assoc-in user [:enablement :end-date] (time/now)))

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

