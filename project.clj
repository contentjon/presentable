(defproject presentable "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :hooks           [leiningen.cljsbuild]
  :plugins         [[lein-cljsbuild "0.3.2"]]
  :dependencies    [[org.clojure/clojure "1.5.1"]]
  :profiles        {:dev {:dependencies [[crate       "0.2.4"]
                                         [jayq        "2.4.0"]
                                         [mocha-latte "0.1.1"]
                                         [chai-latte  "0.1.2"]
                                         [tbn         "0.1.0-SNAPSHOT"]]}}
  :cljsbuild
  {:crossovers     []
   :crossover-path "crossover"
   :builds
   [{:source-paths ["src" "test"],
     :id "test",
     :compiler
     {:pretty-print  true,
      :output-to     "test/unit.js",
      :optimizations :simple}}
    {:source-paths ["src" "examples/menu"],
     :id "menu",
     :compiler
     {:pretty-print  true,
      :output-to     "examples/menu/menu.js",
      :optimizations :simple}}
    {:source-paths ["src" "examples/pie"],
     :id "pie",
     :compiler
     {:pretty-print  true,
      :output-to     "examples/pie/pie.js",
      :optimizations :simple}}
    {:source-paths ["src"],
     :id "api",
     :compiler
     {:pretty-print  true,
      :output-to     "examples/api/presentable.js",
      :optimizations :simple}}]})
