
(defn- first-arg [arg & whatever] arg)

(defn- calculate- [sin-or-cos turn]
  (let [radius (+ 1 (/ turn 360))]
    (* radius (- (sin-or-cos (Math/toRadians turn))))))

(defmulti calculate first-arg)

(defmethod calculate :x [_ turn] (calculate- #(Math/sin %) turn))
(defmethod calculate :y [_ turn] (calculate- #(Math/cos %) turn))

(defn in-rect [rx ry x y]
  (and
    (<= 0 (* rx x))
    (<= 0 (* ry y))
    (<= (Math/abs x) (Math/abs rx))
    (<= (Math/abs y) (Math/abs ry))))

(defn find-under [rx ry seen-dots]
  (first
    (filter identity
      (let [sx (int (Math/signum (float rx)))
            sy (int (Math/signum (float ry)))]
        (for [x (map #(* % sx) (range (+ 1 (Math/floor (Math/abs rx)))))
              y (map #(* % sy) (range (+ 1 (Math/floor (Math/abs ry)))))]
          (and (not (contains? seen-dots [x y]))
            (in-rect rx ry x y)
            [x y]))))))

(defn spiral []
  (let [turn (atom 0)
        seen-dots (atom #{[0 0]})]
    (for [idx (range)]
      (loop [cur-turn @turn]
        (let [new-turn (inc cur-turn)
              old-x (calculate :x cur-turn)
              old-y (calculate :y cur-turn)
              new-x (calculate :x new-turn)
              new-y (calculate :y new-turn)
              new-dot (first (filter identity [(find-under old-x new-y @seen-dots)
                                               (find-under new-x old-y @seen-dots)]))]
          (if new-dot
            (do
                          ; XXX lolwut?
              (swap! turn (fn [x] cur-turn))
              (swap! seen-dots #(conj % new-dot))
              new-dot)
            (recur new-turn)))))))
