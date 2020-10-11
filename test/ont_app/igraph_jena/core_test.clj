(ns ont-app.igraph-jena.core-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.io :as io]
   [ont-app.igraph-jena.core :as core :refer :all]
   [ont-app.rdf.core :as rdf]
   [ont-app.vocabulary.core :as voc]
   [ont-app.igraph.core :refer :all]
   [ont-app.graph-log.core :as glog]
   )
  (:import
   [org.apache.jena.riot RDFDataMgr]
   [org.apache.jena.query Dataset
    QueryExecution
    QueryExecutionFactory
    QueryFactory]
   [org.apache.jena.rdf.model
    Model
    ResourceFactory
    ]
   ))

(glog/log-reset!)
(glog/set-level! :glog/LogGraph :glog/OFF)

(voc/put-ns-meta!
 'com.example.rdf
 {
  :vann/preferredNamespacePrefix "eg"
  :vann/preferredNamespaceUri "http://rdf.example.com#"
  })

(def data (io/file  "test/resources/test-data.ttl"))

(def g (make-jena-graph (RDFDataMgr/loadModel (str data))))

(deftest test-normal-form
  (is (= 
         {:eg/Thing1
          {:eg/number #{"\"1^^http://www.w3.org/2001/XMLSchema#integer\""},
           :rdfs/label #{"\"Thing 1@en\""},
           :rdf/type #{:eg/Thing}},
          :http://rdf.example.com #:rdf{:type #{:eg/TestFile}},
          :eg/Thing2
          {:eg/number #{"\"2^^http://www.w3.org/2001/XMLSchema#integer\""},
           :rdfs/label #{"\"Thing 2@en\""},
           :rdf/type #{:eg/Thing}}}
         (normal-form g))))
;; TODO: We should be interpreting xsd and tagged literals.

(comment 
  (def g (RDFDataMgr/loadModel (str data)))

  (def q "Select * where {?s ?p ?o}")

  (def qe (-> (QueryFactory/create q)
              (QueryExecutionFactory/create g)))

  (def bindings (iterator-seq (.execSelect qe)))


  (def r (query-jena-model g "Select * where {?s ?p ?o}"))

  #_(def s (rdf/query-for-subjects nil query-jena-model g))
  (def s (get-subjects g))

  (def spo (rdf/query-for-normal-form nil query-jena-model  g))

  (def n (get-normal-form g))

  (def gloss-description (do-get-p-o g :http://rdf.naturallexicon.org/en/ont#gloss))

  (def gloss-subPropertyOf (do-get-o g
                                     :http://rdf.naturallexicon.org/en/ont#gloss
                                     :rdfs/subPropertyOf))
  (def answer (do-ask g
                      :http://rdf.naturallexicon.org/en/ont#gloss
                      :rdfs/subPropertyOf
                      :skos/definition))

  (def G (make-jena-graph g))


  (def s (ResourceFactory/createResource
          (voc/uri-for :http://rdf.naturallexicon.org/en/ont#gloss)))
  (def p (ResourceFactory/createProperty
          (voc/uri-for :rdfs/subPropertyOf)))

  (def o (ResourceFactory/createResource
          (voc/uri-for :skos/definition)))

  (def stmt (ResourceFactory/createStatement s p o))

  (def G (add-to-graph G [:http://eg.com/blah :http://eg.com/blah :http://eg.com/blah]))

  (def G (remove-from-graph G [:http://eg.com/blah :http://eg.com/blah :http://eg.com/blah]))

  (def G (add-to-graph G
                       [[:http://eg.com/blah :http://eg.com/blah :http://eg.com/blah]
                        [:http://eg.com/blah :http://eg.com/blah :http://eg.com/blih]]
                       ))


  )
