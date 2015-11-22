(defproject dev-scripts "0.1.0-SNAPSHOT"
  :description "Scrips used to help developers"
  :url "http://c3.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [stax "1.2.0"]]
  :main ^:skip-aot dev-scripts.core
  :target-path "target/%s"
  :eval-in-leiningen true          
  :profiles {:uberjar {:aot :all}})
