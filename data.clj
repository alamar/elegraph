
(defn read-data [file]
  (remove empty?
    (for [row (.split (slurp file) "\n")]
      (vec
        (for [col (.split row "\t")]
          (if (re-matches #"^[0-9]+$" col)
            (Integer/parseInt col)
            col))))))

(defn окно [коррекция значения]
  (let [минимум (apply min (map коррекция значения))
        максимум (apply max (map коррекция значения))]
    (fn [значение]
      (let [корректированное (коррекция значение)]
        (/
          (- корректированное минимум)
          (- максимум минимум))))))
