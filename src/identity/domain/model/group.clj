(ns identity.domain.model.group
  (:require [rill.message :refer [defevent]]
            [rill.aggregate :refer [handle-event]]
            [schema.core :as s]))

(defrecord Group [tenant-id name description])
(defrecord GroupMember [tenant-id name type])

(defn member
  [group name type]
  (let [members (or (:members group) #{})
        member? #(cond (and (= (:type %) type) (= (:name %) name)) %)]
    (some member? members)))

(defn member?
  [group child type]
  (not (nil? (member group child type))))

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

(defevent GroupMemberAdded
          :tenant-id s/Uuid
          :name s/Str
          :child s/Str
          group-id)

(defmethod handle-event ::GroupMemberAdded
  [group {:keys [tenant-id child]}]
  (let [current-members (:members group #{})]
    (assoc group :members (conj current-members (->GroupMember tenant-id child :group)))))

(defevent GroupMemberRemoved
          :tenant-id s/Uuid
          :name s/Str
          :child s/Str)

(defmethod handle-event ::GroupMemberRemoved
  [group {:keys [child]}]
  (let [current-members (:members group #{})
        group-to-remove (member group child :group)]
    (assoc group :members (disj current-members group-to-remove))))

(defevent UserMemberAdded
          :tenant-id s/Uuid
          :name s/Str
          :username s/Str
          group-id)

(defmethod handle-event ::UserMemberAdded
  [group {:keys [tenant-id username]}]
  (let [current-members (:members group #{})]
    (assoc group :members (conj current-members (->GroupMember tenant-id username :user)))))

(defevent UserMemberRemoved
          :tenant-id s/Uuid
          :name s/Str
          :child s/Str)

(defmethod handle-event ::UserMemberRemoved
  [group {:keys [child]}]
  (let [current-members (:members group #{})
        group-to-remove (member group child :user)]
    (assoc group :members (disj current-members group-to-remove))))

