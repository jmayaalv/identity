(ns identity.application.command.user
  (:require [clojure.string :refer [blank?]]
            [rill.aggregate :refer [handle-command aggregate-ids]]
            [rill.message :refer [defcommand primary-aggregate-id]]
            [schema.core :as s]
            [identity.domain.model.notifications :refer [notify-user]]
            [identity.domain.model.tenant :as tenant]
            [identity.domain.model.user :as user])
  (:import (java.util Date)))


(defcommand Register!
            :tenant-id s/Uuid
            :first-name s/Str
            :last-name s/Str
            :email s/Str
              :username s/Str
            :password s/Str
            user/user-id)

;(defmethod primary-aggregate-id ::Register!
;  [{:keys [user-id tenant-id]}]
;  (str user-id ":" tenant-id))
;

(defmethod aggregate-ids ::Register!
  [_ {:keys [tenant-id]}]
  [tenant-id])

(defmethod handle-command ::Register!
  [user {:keys [tenant-id first-name last-name email username password]} tenant]
  (cond
    (tenant/inactive? tenant) [:rejected [:tenant-inactive "Tenant inactive"]]
    (not (nil? user)) [:rejected [:invalid-username "User Already Exists"]]
    (blank? first-name) [:rejected [:blank-first-name "can't be blank"]]
    (blank? last-name) [:rejected [:blank-last-name "can't be blank"]]
    (blank? email) [:rejected [:blank-email "can't be blank"]]
    (blank? username) [:rejected [:blank-username "can't be blank"]]
    (blank? password) [:rejected [:blank-password "can't be blank"]]
    ;todo validate that username is unique on the tenant

    :else
    [:ok [(user/registered tenant-id first-name last-name email username (user/encrypt! password tenant-id))]]))

;(defcommand Authenticate!
;            :tenant-id s/Uuid
;            :user-id s/Uuid
;            :username s/Str
;            :password s/Str)
;
;(defmethod aggregate-ids ::Authenticate!
;  [_ {:keys [user-id tenant-id :as ids]}]
;  ids)
;
;(defmethod handle-command ::Authenticate!
;  [user {:keys [user-id username password]} tenant]
;  (cond (tenant/inactive? tenant) [:rejected [:tenant "Tenant is not active"]]
;        (user/disabled? tenant) [:rejected [:tenant "User is not enabled"]]
;        (user/invalid-credentials? user username password) [:rejected [:user "Invalid Credentials"]]
;
;        :else
;        [:ok [(user/Authenticated user-id)]]))

(defcommand ChangePassword!
            :tenant-id s/Uuid
            :username s/Str
            :current-password s/Str
            :new-password s/Str
            user/user-id)

(defmethod handle-command ::ChangePassword!
  [user {:keys [tenant-id username current-password new-password]}]
  (cond
    (blank? new-password) [:rejected [:password "can't be blank"]]
    (user/invalid-credentials? user (:username user) current-password) [:rejected [:invalid-credentials "Invalid Credentials"]]
    :else
    [:ok [(user/password-changed tenant-id username (user/encrypt! new-password tenant-id))]]))


(defcommand ChangeName!
            :tenant-id s/Uuid
            :username s/Str
            :first-name s/Str
            :last-name s/Str
            user/user-id)

(defmethod handle-command ::ChangeName!
  [_ {:keys [tenant-id username first-name last-name]}]
  (cond
    (blank? first-name) [:rejected [:blank-first-name "First Name can't be blank"]]
    (blank? last-name) [:rejected [:blank-last-name "Last Name can't be blank"]]
    :else
    [:ok [(user/name-changed tenant-id username first-name last-name)]]))

(defcommand DefineEnablement!
            :tenant-id s/Uuid
            :username s/Str
            :start-date Date
            :end-date Date
            user/user-id)

(defmethod handle-command ::DefineEnablement!
  [_ {:keys [tenant-id username start-date end-date]}]
  (cond
    (blank? start-date) [:rejected [:blank-start-date "Start Date can't be blank"]]
    :else
    [:ok [(user/enablement-defined tenant-id username start-date end-date)]]))

(defcommand Enable!
            :tenant-id s/Uuid
            :username s/Str
            user/user-id)

(defmethod aggregate-ids ::Enable!
  [_ {:keys [tenant-id]}]
  [tenant-id])

(defmethod handle-command ::Enable!
  [user {:keys [tenant-id username]} tenant]
  (cond
    (tenant/inactive? tenant) [:rejected [:tenant-inactive "Tenant is not active"]]
    (user/enabled? user) [:rejected [:user-enabled "User is already enabled"]]
    :else
    [:ok [(user/enabled tenant-id username)]]))

(defcommand Disable!
            :tenant-id s/Uuid
            :username s/Str
            user/user-id)

(defmethod aggregate-ids ::Disable!
  [_ {:keys [tenant-id]}]
  [tenant-id])

(defmethod handle-command ::Disable!
  [user {:keys [tenant-id username]} tenant]
  (cond
    (tenant/inactive? tenant) [:rejected [:tenant-inactive "Tenant is not active"]]
    (user/disabled? user) [:rejected [:user-disabled "User is already disabled"]]
    :else
    [:ok [(user/disabled tenant-id username)]]))

(defmethod notify-user ::tenant/Provisioned
  [_ {:keys [tenant-id admin-first-name admin-last-name admin-email admin-username admin-password]} _]
  [(user/registered tenant-id admin-first-name admin-last-name admin-email admin-username admin-password)
   (user/enabled tenant-id admin-username)])