(ns identity.application.command.group
  (:require [clojure.string :refer [blank?]]
            [rill.aggregate :refer [handle-command aggregate-ids]]
            [rill.message :refer [defcommand]]
            [schema.core :as s]
            [identity.domain.model.tenant :as tenant]
            [identity.domain.model.user :as user]
            [identity.domain.model.group :as group]))

(defn group-aggregate-ids
  [{:keys [tenant-id child]}]
  [tenant-id (group/group-id {:tenant-id tenant-id :name child})])

(defn user-aggregate-ids
  [{:keys [tenant-id username]}]
  [tenant-id (user/user-id {:tenant-id tenant-id :username username})])

(defcommand Provision!
            :tenant-id s/Uuid
            :name s/Str
            :description s/Str
            group/group-id)

(defmethod aggregate-ids ::Provision!
  [_ {:keys [tenant-id]}]
  [tenant-id])

(defmethod handle-command ::Provision!
  [group {:keys [tenant-id name description]} tenant]
  (cond
    (tenant/inactive? tenant) [:rejected [:tenant-inactive "Group can't be provisioned to an inactive tenant"]]
    (blank? name) [:rejected [:blank-name "Can't be blank"]]
    (not (nil? group)) [:rejected [:group-exists "Group already exists"]]

    :else
    [:ok [(group/provisioned tenant-id name description)]]))

(defcommand AddGroupMember!
            :tenant-id s/Uuid
            :name s/Str
            :child s/Str
            group/group-id)

(defmethod aggregate-ids ::AddGroupMember!
  [_ c]
  (group-aggregate-ids c))

(defmethod handle-command ::AddGroupMember!
  [group {:keys [tenant-id name child]} tenant child-group]
  (cond
    (tenant/inactive? tenant) [:rejected [:tenant-inactive "Group can't be added to an inactive tenant"]]
    (nil? group) [:rejected [:group-not-exists "Parent group doesn't exists"]]
    (nil? child-group) [:rejected [:group-not-exists "Child group doesn't exists"]]
    (group/member? group child :group) [:rejected [:already-member "Already a member of the group"]]
    :else
    [:ok [(group/group-member-added tenant-id name child)]]))

(defcommand RemoveGroupMember!
            :tenant-id s/Uuid
            :name s/Str
            :child s/Str
            group/group-id)

(defmethod aggregate-ids ::RemoveGroupMember!
  [_ c]
  (group-aggregate-ids c))

(defmethod handle-command ::RemoveGroupMember!
  [group {:keys [tenant-id name child]} tenant child-group]
  (cond
    (tenant/inactive? tenant) [:rejected [:tenant-inactive "Group can't be removed to an inactive tenant"]]
    (nil? group) [:rejected [:group-not-exists "Parent group doesn't exists"]]
    (nil? child-group) [:rejected [:group-not-exists "Child group doesn't exists"]]
    (not (group/member? group child :group)) [:rejected [:not-member "Child is not member of the group"]]
    :else
    [:ok [(group/group-member-removed tenant-id name child)]]))


(defcommand AddUserMember!
            :tenant-id s/Uuid
            :name s/Str
            :username s/Str
            group/group-id)

(defmethod aggregate-ids ::AddUserMember!
  [_ c]
  (user-aggregate-ids c))

(defmethod handle-command ::AddUserMember!
  [group {:keys [tenant-id name username]} tenant user]
  (cond
    (tenant/inactive? tenant) [:rejected [:tenant-inactive "Group can't be added to an inactive tenant"]]
    (nil? group) [:rejected [:group-not-exists "Parent group doesn't exists"]]
    (nil? user) [:rejected [:user-not-exists "User doesn't exists"]]
    (group/member? group username :user) [:rejected [:already-member "Alread a member of the group"]]
    :else
    [:ok [(group/user-member-added tenant-id name username)]]))

(defcommand RemoveUserMember!
            :tenant-id s/Uuid
            :name s/Str
            :username s/Str
            group/group-id)

(defmethod aggregate-ids ::RemoveUserMember!
  [_ c]
  (user-aggregate-ids c))

(defmethod handle-command ::RemoveUserMember!
  [group {:keys [tenant-id name username]} tenant user]
  (cond
    (tenant/inactive? tenant) [:rejected [:tenant-inactive "Group can't be removed on an inactive tenant"]]
    (nil? group) [:rejected [:group-not-exists "Parent group doesn't exists"]]
    (nil? user) [:rejected [:group-not-exists "Child user doesn't exists"]]
    (not (group/member? group username :user)) [:rejected [:not-member "Child is not member of the group"]]
    :else
    [:ok [(group/user-member-removed tenant-id name username)]]))
