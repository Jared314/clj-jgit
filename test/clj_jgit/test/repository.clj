(ns clj-jgit.test.repository
  (:require [clojure.test :refer :all]
  			    [clj-jgit.test.helpers :as helpers]
            [clj-jgit.repository :as r]
            [clj-jgit.porcelain :as c]
            [clojure.string :as string])
  (:import [java.util Date]))

(def test-repo-path helpers/read-only-repo-path)

(deftest read-repo-test
  (let [testrepo (c/load-repo test-repo-path)
        x (r/get-repo test-repo-path)]
    (testing "Reading from the repository"
      ;; Equality testing
      (is (= x
             (r/get-repo test-repo-path)))

      ;; Checking for a commit by id
      (is (contains? x "38dd57264cf5c05fb77211c8347d1f16e4474623"))
      (is (contains? x "HEAD"))

      ;; Fetching a commit
      (is (= (get x "38dd57264cf5c05fb772")
             (x "38dd57264cf5c05fb772")))
      (is (= (get x "HEAD")
             (x "HEAD")))

      ;; Fetching a commit with a notFound parameter
      (is (= :fail
             (get x "" :fail)
             (x "" :fail)))

      ;; The repository has the following default metadata
      (is (= {:clj-jgit.repository/path test-repo-path,
              :clj-jgit.repository/branch (c/git-branch-current testrepo)
              :clj-jgit.repository/branches (map #(string/replace (.getName %) #"^refs/heads/" "") (c/git-branch-list testrepo))
              :clj-jgit.repository/attached-head true}
             (meta x)))

      )))
