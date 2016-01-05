(ns identity.application.command.role
  (:require [clojure.string :refer [blank?]]
            [rill.aggregate :refer [handle-command aggregate-ids]]
            [rill.message :refer [defcommand]]
            [schema.core :as s]
            [identity.domain.model.notifications :refer [notify-role]]
            [identity.domain.model.tenant :as tenant]
            [identity.domain.model.role :as role]
            [identity.domain.model.group :as group]
            [identity.domain.model.user :as user]))

(defcommand Provision!
            :tenant-id s/Uuid
            :role-name s/Str
            :description s/Str
            :supports-nesting s/Bool
            role/role-id)

(defmethod aggregate-ids ::Provision!
  [_ {:keys [tenant-id]}]
  [tenant-id])

(defmethod handle-command ::Provision!
  [role {:keys [tenant-id role-name description supports-nesting]} tenant]
  (let [group-name (group/new-internal-group-name)]
    (cond
      (tenant/inactive? tenant) [:rejected [:tenant-inactive "Tenant inactive"]]
      (nil? tenant) [:rejected [:tenant-not-found "Tenant doesn't exist"]]
      (not (nil? role)) [:rejected [:role-already-exists "Role already exists"]]
      (blank? role-name) [:rejected [:blank-role-name "Role name is blank"]]
    :else
    [:ok [(role/provisioned tenant-id role-name description group-name supports-nesting)]])))

(defcommand AssignUser!
            :tenant-id s/Uuid
            :role-name s/Str
            :username s/Str
            role/role-id)

(defmethod aggregate-ids ::AssignUser!
  [_ {:keys [tenant-id username]}]
  [tenant-id
   (user/user-id {:tenant-id tenant-id :username username})])

(defmethod handle-command ::AssignUser!
  [role {:keys [tenant-id role-name username]} tenant user]
  (cond
    (nil? tenant) [:rejected [:tenant-not-found "Tenant doesn't exist"]]
    (nil? role) [:rejected [:role-not-found "Role doesn't exist"]]
    (nil? user) [:rejected [:user-not-found "User doesn't exist"]]
    :else
    [:ok [(role/user-assigned tenant-id role-name username)]]))

(defcommand UnassignUser!
            :tenant-id s/Uuid
            :role-name s/Str
            :username s/Str
            role/role-id)

(defmethod aggregate-ids ::UnassignUser!
  [_ {:keys [tenant-id username]}]
  [tenant-id
   (user/user-id {:tenant-id tenant-id :userame username})])

(defmethod handle-command ::UnassignUser!
  [role {:keys [tenant-id role-name username]} tenant user]
  (cond
    (nil? tenant) [:rejected [:tenant-not-found "Tenant doesn't exist"]]
    (nil? role) [:rejected [:role-not-found "Role doesn't exist"]]
    (nil? user) [:rejected [:user-not-found "User doesn't exist"]]
    :else
    [:ok [(role/user-unassigned tenant-id role-name username)]]))

(defcommand AssignGroup!
            :tenant-id s/Uuid
            :role-name s/Str
            :group-name s/Str
            role/role-id)

(defmethod aggregate-ids ::AssignGroup!
  [_ {:keys [tenant-id group-name]}]
  [tenant-id
   (group/group-id {:tenant-id tenant-id :name group-name})])

(defmethod handle-command ::AssignGroup!
  [role {:keys [tenant-id role-name group-name]} tenant group]
  (cond
    (nil? tenant) [:rejected [:tenant-not-found "Tenant doesn't exist"]]
    (nil? role) [:rejected [:role-not-found "Role doesn't exist"]]
    (nil? group) [:rejected [:group-not-found "Group doesn't exist"]]
    (:supports-nesting role) [:nesting-not-supported "This role does not support group nesting."]
    :else
   [:ok [(role/group-assigned tenant-id role-name group-name)]]))

(defcommand UnassignGroup!
            :tenant-id s/Uuid
            :role-name s/Str
            :group-name s/Str
            role/role-id)

(defmethod aggregate-ids ::UnassignGroup!
  [_ {:keys [tenant-id group-name]}]
  [tenant-id
   (group/group-id {:tenant-id tenant-id :name group-name})])

(defmethod handle-command ::UnassignGroup!
  [role {:keys [tenant-id role-name group-name]} tenant group]
  (cond
    (nil? tenant) [:rejected [:tenant-not-found "Tenant doesn't exist"]]
    (nil? role) [:rejected [:role-not-found "Role doesn't exist"]]
    (nil? group) [:rejected [:group-not-found "Group doesn't exist"]]
    (:supports-nesting role) [:nesting-not-supported "This role does not support group nesting."]
    :else
    [:ok [(role/group-unassigned tenant-id role-name group-name)]]))

(defmethod notify-role ::tenant/Provisioned
  [_ {:keys [tenant-id]} _]
  (let [group-name (group/new-internal-group-name)]
    [(role/created tenant-id role/admin-role-name nil group-name false)]))