(ns identity.domain.model.tenant
  (:require [schema.core :as s]
            [rill.message :refer [defevent]]
            [rill.aggregate :refer [handle-event]]))

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
          :description s/Str)

(defmethod handle-event ::Provisioned
  [_ {:keys [tenant-id name description]}]
  (->Tenant tenant-id name description))

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