(ns identity.domain.model.role
  (:require [rill.message :refer [defevent]]
            [rill.aggregate :refer [handle-event]]
            [schema.core :as s]))

(defrecord Role [tenant-id name description supports-nesting])

(defn role-id [{:keys [tenant-id name]}]
  (str "role: " tenant-id ":" name))

(defevent Provisioned
          :tenant-id s/Uuid
          :name s/Str
          :description s/Str
          :supports-nesting s/Bool)

(defmethod handle-event ::Provisioned
  [_ {:keys [tenant-id name description supports-nesting]}]
  (->Role tenant-id name description supports-nesting))

