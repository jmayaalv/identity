(ns identity.domain.model.role
  (:require [rill.message :refer [defevent observers]]
            [rill.aggregate :refer [handle-event aggregate-ids load-aggregate]]
            [rill.event-store :refer [retrieve-events]]
            [schema.core :as s]
            [identity.domain.model.notifications :refer [notify-group]]
            [identity.domain.model.group :as group]))

(defrecord Role [tenant-id name description group-name supports-nesting])

(def admin-role-name "Admin")

(defn role-id [{:keys [tenant-id role-name]}]
  (str "role: " tenant-id ":" role-name))

;;FIXME: Better to use Projections instead of aggregates
(defn in-role?
  [{:keys [group-name]} {:keys [tenant-id username]} store]
  (let [group (load-aggregate
                (retrieve-events store (group/group-id {:tenant-id tenant-id :name group-name})))]
    (and (not (nil? group))
         (group/member? group username :user))))

(defn assign-aggregate-ids
  [role tenant-id]
  [(group/group-id {:tenant-id tenant-id :name (:group-name role)})])

(defevent Provisioned
          :tenant-id s/Uuid
          :role-name s/Str
          :description s/Str
          :group-name s/Str
          :supports-nesting s/Bool
          role-id)

(defmethod handle-event ::Provisioned
  [_ {:keys [tenant-id role-name description group-name supports-nesting]}]
  (->Role tenant-id role-name description group-name supports-nesting))

(defevent Created
          :tenant-id s/Uuid
          :role-name s/Str
          :description s/Str
          :group-name s/Str
          :supports-nesting s/Bool
          role-id)

(defmethod handle-event ::Created
  [_ {:keys [tenant-id role-name description group-name supports-nesting]}]
  (->Role tenant-id role-name description group-name supports-nesting))

(defmethod observers ::Provisioned
  [_]
  [[nil notify-group]])

(defevent UserAssigned
          :tenant-id s/Uuid
          :role-name s/Str
          :username s/Str
          role-id)

(defmethod observers ::UserAssigned
  [{:keys [tenant-id role-name]}]
  [[(role-id {:tenant-id tenant-id :role-name role-name}) notify-group]])

(defmethod handle-event ::UserAssigned
  [role _]
  role)

(defevent UserUnassigned
          :tenant-id s/Uuid
          :role-name s/Str
          :username s/Str
          role-id)

(defmethod aggregate-ids ::UserUnassigned
  [role {:keys [tenant-id]}]
  [(assign-aggregate-ids role tenant-id)])

(defmethod handle-event ::UserUnassigned
  [_ {:keys [username]} group]
  (group/remove-user-member group username))

(defevent GroupAssigned
          :tenant-id s/Uuid
          :role-name s/Str
          :group-name s/Str
          role-id)

(defmethod aggregate-ids ::GroupAssigned
  [role {:keys [tenant-id]}]
  [(assign-aggregate-ids role tenant-id)])

(defmethod handle-event ::GroupAssigned
  [_ {:keys [tenant-id group-name]} group]
  (group/add-group-member tenant-id group group-name))

(defevent GroupUnassigned
          :tenant-id s/Uuid
          :role-name s/Str
          :group-name s/Str
          role-id)

(defmethod aggregate-ids ::GroupUnassigned
  [role {:keys [tenant-id]}]
  [(assign-aggregate-ids role tenant-id)])

(defmethod handle-event ::GroupUnassigned
  [_ {:keys [group-name]} group]
  (group/remove-group-member group group-name))