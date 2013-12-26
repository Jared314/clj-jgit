(ns clj-jgit.repository
  (:require [clj-jgit.porcelain :as c]
            [clj-jgit.querying :as q]
            [clj-jgit.internal :as i]
            [clojure.string :as string])
  (:import [clojure.lang ISeq Associative IFn MapEntry IObj]
           [org.eclipse.jgit.api Git]
           [org.eclipse.jgit.revwalk RevWalk]))

(defn gen-metadata [^Git repo]
  (let [path (-> repo .getRepository .getDirectory .getParent)
        branch (c/git-branch-current repo)
        branch-names (map #(string/replace (.getName %) #"^refs/heads/" "") (c/git-branch-list repo))
        attached (c/git-branch-attached? repo)]
    {::path path
     ::branch branch
     ::branches branch-names
     ::attached-head attached}))

(deftype Repository [^Git repo ^RevWalk walker metadata]
  Associative
  (count [this] -1) ;; TODO
  (seq [this] (seq walker))
  (cons [this o] this)
  (empty [_] nil)
  (equiv [this o]
         (and (instance? Repository o)
              (= (select-keys (.meta this) [::path ::branch])
                 (select-keys (meta o) [::path ::branch]))))
  (containsKey [_ key] (not (nil? (i/resolve-object key repo))))
  (entryAt [this k] (MapEntry. k (.valAt this k)))
  (assoc [this _ _] this)
  (valAt [this key] (.valAt this key nil))
  (valAt [this key notFound] (if (.containsKey this key)
                               (q/find-rev-commit repo walker key)
                               notFound))
  IFn
  (invoke [this arg1] (.valAt this arg1 nil))
  (invoke [this arg1 arg2] (.valAt this arg1 arg2))
  IObj
  (withMeta [_ meta] (Repository. repo walker (merge metadata meta)))
  (meta [_] (merge (gen-metadata repo) metadata))
  Object
  (toString [this] (let [m (.meta this)] (str "Path: " (::path m) " Branch: " (::branch m)))))

(defn get-repo [path]
  (c/with-repo path (Repository. repo rev-walk nil)))
