(ns identity.application.command.tenant
  (:require [clojure.string :refer [blank?]]
            [rill.aggregate :refer [handle-command aggregate-ids]]
            [rill.message :refer [defcommand primary-aggregate-id]]
            [schema.core :as s]
            [identity.domain.model.tenant :as tenant]
            [identity.domain.model.user :as user]
            [identity.infrastructure.common :refer [new-uuid]]))

(defcommand Provision!
            :tenant-id s/Uuid
            :name s/Str
            :description s/Str
            :admin-first-name s/Str
            :admin-last-name s/Str
            :admin-email s/Str
            :admin-username s/Str
            :admin-password s/Str)

(defmethod handle-command ::Provision!
  [_ {:keys [tenant-id name description admin-first-name admin-last-name admin-email admin-username admin-password]}]
  (cond
    (blank? name) [:rejected [:name "Can't be blank"]]
    :else
    [:ok [(tenant/provisioned tenant-id name description admin-first-name admin-last-name admin-email admin-username
                              (user/encrypt! admin-password tenant-id))
          (tenant/activated tenant-id)]]))

(defcommand Activate!
            :tenant-id s/Uuid)

(defmethod handle-command ::Activate!
  [tenant {:keys [tenant-id]}]
  (cond
    (tenant/active? tenant) [:rejected [:tenant "Tenant is already active"]]

    :else
    [:ok [(tenant/activated tenant-id)]]))

(defcommand Deactivate!
            :tenant-id s/Uuid)

(defmethod handle-command ::Deactivate!
  [tenant {:keys [tenant-id]}]
  (cond
    (tenant/inactive? tenant) [:rejected [:tenant "Tenant is already inactive"]]

    :else
    [:ok [(tenant/deactivated tenant-id)]]))


(defcommand ChangeName!
            :tenant-id s/Uuid
            :name s/Str)

(defmethod handle-command ::ChangeName!
  [_ {:keys [tenant-id name]}]
  (cond
    (blank? name) [:rejected [:name "Can't be blank"]]
    :else
    [:ok [(tenant/name-changed tenant-id name)]]))

(defcommand ChangeDescription!
            :tenant-id s/Uuid
            :description s/Str)

(defmethod handle-command ::ChangeDescription!
  [_ {:keys [tenant-id description]}]
    [:ok [(tenant/description-changed tenant-id description)]])
