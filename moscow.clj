(ns ru.lj.alamar.elegraph
  (:require spiral data image))

(defn сумма [список] (apply + список))

(defn zip [keys vals]
  (loop [seqq []
         ks (seq keys)
         vs (seq vals)]
    (if (and ks vs)
      (recur (conj seqq [(first ks) (first vs)])
        (next ks) (next vs))
      seqq)))

(def данные (read-data "data/moscow-2011-12-07/data.csv"))
(def заголовки (first данные))
(def уики (rest данные))

(defn партии [уик]
  (subvec уик 19))

(def избиратели second)

(defn явка [уик]
  (/ (избиратели уик) (сумма (партии уик))))

(defn доля-ер [уик]
  (/ (избиратели уик) (nth уик 24)))

(def окно-явки (window явка уики))
(def окно-ер (window доля-ер уики))

(def ширина 6000)
(def высота 8000)

(def отступ 500) 

(def размер-графика 5000)

(def холст (create-image ширина высота))

(def шаблон (spiral))

(def цвета [(цвет 0x33 0x33 0x9A) ;; Справедливая Россия
            (цвет 0xFF 0xFF 0)    ;; ЛДПР
            (цвет 0x9A 0x33 0x9A) ;; Патриоты России
            (цвет 0xCC 0 0)       ;; КПРФ
            (цвет 0x33 0x99 0)    ;; Яблоко
            (цвет 0 0 0)          ;; Единая Россия
            (цвет 0x33 0x9A 0x9A) ;; Правое Дело
            (цвет 0xCC 0xCC 0xCC) ;; Неявка
           ])

(defn выделить-фракции [уик]
  (let [все-партии (партии уик)]
    (vec (conj все-партии (- (избиратели уик) (сумма все-партии))))))

(doseq [уик (take 300 уики)]
  (when-let [смещение
             [(int (* размер-графика (window-weight явка уик окно-явки)))
              (int (* размер-графика (window-weight доля-ер уик окно-ер)))]]
    (нанести холст отступ размер-графика смещение шаблон (zip (выделить-фракции уик) цвета))
    (println "Обработан " (first уик))))

(save-image холст "moscow.png")

