(ns identity.domain.model.group
  (:require [rill.message :refer [defevent]]
            [rill.aggregate :refer [handle-event]]
            [schema.core :as s]
            [identity.infrastructure.common :refer [new-uuid]]))

(defrecord Group [tenant-id name description])
(defrecord GroupMember [tenant-id name type])

(defn new-internal-group-name []
  (str "ROLE-INTERNAL-GROUP: " (new-uuid)))


(defn member
  [group name type]
  (let [members (or (:members group) #{})
        member? #(cond (and (= (:type %) type) (= (:name %) name)) %)]
    (some member? members)))

(defn member?
  [group child type]
  (not (nil? (member group child type))))

(defn- add-member
  [tenant-id group child type]
  (let [current-members (:members group #{})]
    (assoc group :members (conj current-members (->GroupMember tenant-id child type)))))

(defn add-group-member
  [tenant-id group child]
  (add-member tenant-id group child :group))

(defn add-user-member
  [tenant-id group child]
  (add-member tenant-id group child :user))

(defn- remove-member
  [group child type]
  (let [current-members (:members group #{})
        to-remove (member group child type)]
    (assoc group :members (disj current-members to-remove))))


(defn remove-group-member [group child]
  (remove-member group child :group))

(defn remove-user-member [group child]
  (remove-member group child :user))

(defn group-id
  [{:keys [tenant-id name]}]
  (str "group: " tenant-id ":" name))

(defevent Provisioned
          :tenant-id s/Uuid
          :name s/Str
          :description s/Str
          group-id)

(defmethod handle-event ::Provisioned
  [_ {:keys [tenant-id name description]}]
  (->Group tenant-id name description))

(defevent Created
          :tenant-id s/Uuid
          :name s/Str
          :description s/Str
          group-id)

(defmethod handle-event ::Created
  [_ {:keys [tenant-id name description]}]
  (->Group tenant-id name description))

(defevent GroupMemberAdded
          :tenant-id s/Uuid
          :name s/Str
          :child s/Str
          group-id)

(defmethod handle-event ::GroupMemberAdded
  [group {:keys [tenant-id child]}]
  (add-group-member tenant-id group child))

(defevent GroupMemberRemoved
          :tenant-id s/Uuid
          :name s/Str
          :child s/Str
          group-id)

(defmethod handle-event ::GroupMemberRemoved
  [group {:keys [child]}]
  (remove-group-member group child))

(defevent UserMemberAdded
          :tenant-id s/Uuid
          :name s/Str
          :username s/Str
          group-id)

(defmethod handle-event ::UserMemberAdded
  [group {:keys [tenant-id name username]}]
  (add-user-member tenant-id group username))

(defevent UserMemberRemoved
          :tenant-id s/Uuid
          :name s/Str
          :username s/Str
          group-id)

(defmethod handle-event ::UserMemberRemoved
  [group {:keys [username]}]
  (remove-user-member group username))
