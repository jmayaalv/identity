(defproject identity "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [environ "1.0.1"]
                 [io.aviso/pretty "0.1.20"]
                 [clj-time "0.11.0"]
                 [rill-event-sourcing/rill.event_store "0.2.1"]
                 [rill-event-sourcing/rill.handler "0.2.1"]]
  :plugins [[lein-environ "1.0.1"]]
  :main ^:skip-aot identity.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev           [:project/dev :profiles/dev]
             :test          [:project/test :profiles/test]
             :project/dev  {:dependencies [[rill-event-sourcing/rill.event_store.memory "0.2.1"]]}
             :project/test  {:dependencies [[rill-event-sourcing/rill.temp_store "0.2.1"]]}})
