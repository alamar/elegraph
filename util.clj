
(defn сумма [список] (apply + список))

(defn zip [keys vals]
  (loop [seqq []
         ks (seq keys)
         vs (seq vals)]
    (if (and ks vs)
      (recur (conj seqq [(first ks) (first vs)])
        (next ks) (next vs))
      seqq)))

