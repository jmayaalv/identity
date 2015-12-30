(ns identity.identity_test
  (:use clojure.test)
  (:require [rill.event-store.memory :refer [memory-store]]
            [rill.repository :refer [wrap-basic-repository]]
            [rill.aggregate :refer [load-aggregate]]
            [rill.event-store :refer [retrieve-events]]
            [identity.identity :as identity]
            [identity.domain.model.user :as user]
            [identity.infrastructure.common :refer [new-uuid]]))

(deftest test-provision-tenant
  (testing "Tenant is provisioned"
    (let [tenant-id (new-uuid)
          name "A tenant"
          description "The tenant description"
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sauze.com"
          username "kaiser"
          password "thepwass"
          store (wrap-basic-repository (memory-store))
          _ (identity/setup! store)
          _ (identity/provision-tenant! tenant-id name description
                                        user-id first-name last-name email username password)
          tenant (load-aggregate (retrieve-events store tenant-id))
          user (load-aggregate (retrieve-events store user-id))]
      (is (= name (:name tenant)))
      (is (= description (:description tenant)))
      (is (= first-name (:first-name user)))
      (is (= last-name (:last-name user)))
      (is (= username (:username user)))
      (is (= (user/encrypt! password user-id) (:password user)))))

)
