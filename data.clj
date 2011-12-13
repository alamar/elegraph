
(defn read-data [file]
  (remove empty?
    (for [row (.split (slurp file) "\n")]
      (vec
        (for [col (.split row "\t")]
          (if (re-matches #"^[0-9]+$" col)
            (Integer/parseInt col)
            col))))))

(defn window [fn data]
  [(apply min (map fn data)) (apply max (map fn data))])

(defn window-weight [value window]
  (/
    (- value (first window))
    (- (second window) (first window))))
