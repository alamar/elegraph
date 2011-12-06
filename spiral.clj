(ns ru.lj.alamar.elegraph)

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

(defn spiral [n]
  (let [turn (ref 0)
        seen-dots (atom (transient #{[0 0]}))]
    (for [idx (range n)]
      (loop [cur-turn @turn]
        (let [new-turn (inc cur-turn)
              old-x (calculate :x cur-turn)
              old-y (calculate :y cur-turn)
              new-x (calculate :x new-turn)
              new-y (calculate :y new-turn)]
          (for [x (range)
                y (range x)]
            (if
              (and (not (contains? @seen-dots [x y]))
                (or (in-rect old-x new-y x y)
                  (in-rect new-x old-y x y)))
              (do
                (ref-set turn cur-turn)
                (swap! seen-dots #(conj % [x y]))
                [x y])
              (recur new-turn))))))))
