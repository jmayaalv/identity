(ns identity.domain.model.notifications
  (:require [rill.message :as message]))

(defmulti notify-tenant
          (fn [primary-aggregate event & aggregates]
            (message/type event)))

(defmulti notify-user
          (fn [primary-aggregate event & aggregates]
            (message/type event)))

(defmulti notify-group
          (fn [primary-aggregate event & aggregates]
            (message/type event)))

(defmulti notify-role
          (fn [primary-aggregate event & aggregates]
            (message/type event)))
