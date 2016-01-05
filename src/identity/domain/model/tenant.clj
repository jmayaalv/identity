(ns identity.domain.model.tenant
  (:require [schema.core :as s]
            [rill.message :refer [defevent observers]]
            [rill.aggregate :refer [handle-event]]
            [identity.domain.model.notifications :refer [notify-user notify-role notify-group]]
            [identity.domain.model.role :as role]))

(defrecord Tenant [tenant-id name description])

(defn active?
  "Determines if a Tenant is active"
  [{:keys [active]}]
  active)

(defn inactive?
  [tenant]
  ((complement active?) tenant))

(defevent Provisioned
          :tenant-id s/Uuid
          :name s/Str
          :description s/Str
          :admin-first-name s/Str
          :admin-last-name s/Str
          :admin-email s/Str
          :admin-username s/Str
          :admin-password s/Str)

(defmethod handle-event ::Provisioned
  [_ {:keys [tenant-id name description]}]
  (->Tenant tenant-id name description))

(defmethod observers ::Provisioned
  [{:keys [tenant-id]}]
  [[tenant-id notify-user]
   [tenant-id notify-role]
   [(role/role-id {:tenant-id tenant-id :role-name role/admin-role-name}) notify-group]])

(defevent Activated
          :tenant-id s/Uuid)

(defmethod handle-event ::Activated
  [tenant _]
  (assoc tenant :active true))

(defevent Deactivated
          :tenant-id s/Uuid)

(defmethod handle-event ::Deactivated
  [tenant _]
  (assoc tenant :active false))

(defevent NameChanged
          :tenant-id s/Uuid
          :name s/Str)

(defmethod handle-event ::NameChanged
  [tenant {:keys [name]}]
  (assoc tenant :name name))


(defevent DescriptionChanged
          :tenant-id s/Uuid
          :description s/Str)

(defmethod handle-event ::DescriptionChanged
  [tenant {:keys [description]}]
  (assoc tenant :description description))