(ns identity.application.command.user
  (:require [clojure.string :refer [blank?]]
            [rill.aggregate :refer [handle-command aggregate-ids]]
            [rill.message :refer [defcommand]]
            [schema.core :as s]
            [identity.domain.model.tenant :as tenant]
            [identity.domain.model.user :as user])
  (:import (java.util Date)))


(defcommand Register!
            :tenant-id s/Uuid
            :user-id s/Uuid
            :first-name s/Str
            :last-name s/Str
            :email s/Str
            :username s/Str
            :password s/Str)

(defmethod handle-command ::Register!
  [_ {:keys [tenant-id user-id first-name last-name email username password]}]
  (cond
    (blank? first-name) [:rejected [:first-name "can't be blank"]]
    (blank? last-name) [:rejected [:last-name "can't be blank"]]
    (blank? email) [:rejected [:email "can't be blank"]]
    (blank? username) [:rejected [:username "can't be blank"]]
    (blank? password) [:rejected [:password "can't be blank"]]
    ;todo validate that username is unique on the tenant

    :else
    [:ok [(user/registered tenant-id user-id first-name last-name email username (user/encrypt! password user-id))]]))

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

(defcommand ChangeEmail!
            :user-id s/Str
            :email s/Str)

(defmethod handle-command ::ChangeEmail!
  [user {:keys [user-id email]}]
  (cond
    (= email (:email user)) [:rejected [:user "Email is the same as previous emails"]]
    :else
    [:ok [(user/email-changed user-id email)]]))

(defcommand ChangePassword!
            :user-id s/Uuid
            :current-password s/Str
            :new-password s/Str)

(defmethod handle-command ::ChangePassword!
  [user {:keys [user-id current-password new-password]}]
  (cond
    (blank? new-password) [:rejected [:password "Password can't be blank"]]
    (user/invalid-credentials? user (:username user) current-password) [:rejected [:user "Invalid Credentials"]]
    :else
    [:ok [(user/password-changed user-id (user/encrypt! new-password user-id))]]))


(defcommand ChangeName!
            :user-id s/Uuid
            :first-name s/Str
            :last-name s/Str)

(defmethod handle-command ::ChangeName!
  [_ {:keys [user-id first-name last-name]}]
  (cond
    (blank? first-name) [:rejected [:first-name "First Name can't be blank"]]
    (blank? last-name) [:rejected [:last-name "Last Name can't be blank"]]
    :else
    [:ok [(user/name-changed user-id first-name last-name)]]))

(defcommand DefineEnablement!
            :user-id s/Uuid
            :start-date Date
            :end-date Date)

(defmethod handle-command ::DefineEnablement!
  [_ {:keys [user-id start-date end-date]}]
  (cond
    (blank? start-date) [:rejected [:start-date "Start Date can't be blank"]]
    :else
    [:ok [(user/enablement-defined user-id start-date end-date)]]))

(defcommand Enable!
            :user-id s/Uuid)

(defmethod handle-command ::Enable!
  [user {:keys [user-id]}]
  (cond
    (user/enabled? user) [:rejected [:user "User is already enabled"]]
    :else
    [:ok [(user/enabled user-id)]]))


(defcommand Disable!
            :user-id s/Uuid)

(defmethod handle-command ::Disable!
  [user {:keys [user-id]}]
  (cond
    (user/disabled? user) [:rejected [:user "User is already disabled"]]
    :else
    [:ok [(user/disabled user-id)]]))