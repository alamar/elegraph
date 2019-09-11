
(import '(java.util HashSet))

(defn- calculate- ^Integer [sin-or-cos turn]
  (let [radius (+ 1 (/ turn 360))]
    (int (* radius (- (sin-or-cos (Math/toRadians turn)))))))

(defn calculate-x ^Integer [turn] (calculate- #(Math/sin %) turn))
(defn calculate-y ^Integer [turn] (calculate- #(Math/cos %) turn))

(defn in-rect [^Integer rx ^Integer ry ^Integer x ^Integer y]
  (and
    (<= 0 (* rx x))
    (<= 0 (* ry y))
    (<= (Math/abs x) (Math/abs rx))
    (<= (Math/abs y) (Math/abs ry))))

(defn find-under [^Integer rx ^Integer ry ^HashSet seen-dots]
  (first
    (filter identity
      (let [sx (int (Math/signum (float rx)))
            sy (int (Math/signum (float ry)))]
        (for [x (map #(int (* % sx)) (range (+ 1 (Math/floor (Math/abs rx)))))
              y (map #(int (* % sy)) (range (+ 1 (Math/floor (Math/abs ry)))))]
          (and (not (.contains seen-dots [x y]))
            (in-rect rx ry x y)
            [x y]))))))

(defn spiral []
  (let [turn (atom 0)
        seen-dots (HashSet.)]
    (for [idx (range)]
      (loop [cur-turn @turn]
        (let [new-turn (inc cur-turn)
              old-x (calculate-x cur-turn)
              old-y (calculate-y cur-turn)
              new-x (calculate-x new-turn)
              new-y (calculate-y new-turn)
              new-dot (first (filter identity [(find-under old-x new-y seen-dots)
                                               (find-under new-x old-y seen-dots)]))]
          (if new-dot
            (do
                          ; XXX lolwut?
              (swap! turn (fn [x] cur-turn))
              (.add seen-dots new-dot)
;;              (swap! seen-dots #(HashSet. (conj % new-dot)))
              new-dot)
            (recur new-turn)))))))
