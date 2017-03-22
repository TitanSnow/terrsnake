"
  terrsnake - A snake game demo written in Clojure

  Copyright (C) 2017 TitanSnow

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.

  TitanSnow sweeto@live.cn
"

(ns tttnns.snake
 (:import (java.awt Color Dimension)
          (javax.swing JPanel JFrame Timer JOptionPane)
          (java.awt.event ActionListener KeyListener))
 (:use stuart.import-static))
(import-static java.awt.event.KeyEvent VK_H VK_J VK_K VK_L)
(def w 20)
(def h 20)
(def scl 20)
(def tik 300)
(def snkc (Color. 0 0 0))
(def blkc (Color. 255 255 255))
(def apsc (Color. 255 0 0))
(def k2d
 {VK_H [-1 0]
  VK_J [0 1]
  VK_K [0 -1]
  VK_L [1 0]})
(def ratiks (atom 5))
(def raps (atom #{}))
(defn- addp [& pts]
 (vec (apply map + pts)))
(defn- p2r [pt]
 [(pt 0) (pt 1) 1 1])
(defn- csnk []
 {:b (reverse (for [x (range 6)] [x (quot h 2)]))
  :d [1 0]
  :t :snk})
(defn- icsnk [snk]
 (assoc snk :b (cons (addp (first (:b snk)) (:d snk)) (:b snk))))
(defn- mvsnk [snk]
 (assoc (assoc snk :l (last (:b snk))) :b (drop-last (:b (icsnk snk)))))
(defn- tnsnk [snk dir]
 (assoc snk :d dir))
(defn- ouch? [{b :b}]
 (or (contains? (set (rest b)) (first b))
  (let [[x y] (first b)]
   (or (> x w) (< x 0) (> y h) (< y 0)))))
(defn- obtn? [[a b] [c d]]
 (or (and (= a c) (= b (- d))) (and (= b d) (= a (- c)))))
(def rsnk (atom (csnk)))
(defn- icsnk! [rsnk]
 (swap! rsnk icsnk))
(defn- mvsnk! [rsnk]
 (swap! rsnk mvsnk))
(defn- tnsnk! [rsnk dir]
 (swap! rsnk tnsnk dir))
(defn- upst! []
 (if (contains? @raps (first (:b @rsnk)))
  (do
   (swap! raps disj (first (:b @rsnk)))
   (icsnk! rsnk))
  (mvsnk! rsnk))
 (if (zero? @ratiks)
  (do
   (reset! ratiks 5)
   (swap! raps conj (:l @rsnk)))
  (swap! ratiks dec)))
(defn- uptn! [nd]
 (when (and nd (not (obtn? (:d @rsnk) nd))) (tnsnk! rsnk nd)))
(defn- resetgame! []
 (def rsnk (atom (csnk)))
 (def raps (atom #{}))
 (reset! ratiks 5))
(defn- pp [g pt c]
 (let [[x y w h] (p2r pt)]
  (.setColor g c)
  (.fillRect g x y w h)))
(defn- paint [g {b :b}]
 (doseq [pt b] (pp g pt snkc)))
(defn- paintaps [g aps]
 (doseq [pt aps] (pp g pt apsc)))
(defn- clearpaint [g snk]
 (.setColor g blkc)
 (if (not (contains? snk :l)) (.fillRect g 0 0 (inc w) (inc h))
  (let [[x y w h] (p2r (:l snk))]
   (.fillRect g x y w h))))
(defn game [] (let [frame (JFrame. "SNAKE")
      panel (doto (proxy [JPanel ActionListener KeyListener] []
                   (paintComponent [g]
                     (.scale g scl scl)
                     (clearpaint g @rsnk)
                     (paintaps g @raps)
                     (paint g @rsnk))
                   (actionPerformed [e]
                    (upst!)
                    (when (ouch? @rsnk)
                     (do (JOptionPane/showMessageDialog frame "OUCH!")
                     (resetgame!)))
                    (.repaint this))
                   (keyPressed [e]
                    (uptn! (k2d (.getKeyCode e))))
                   (keyReleased [e])
                   (keyTyped [e]))
             (.setPreferredSize (Dimension. (* scl (inc w)) (* scl (inc h)))))
      _ (doto panel
         (.setFocusable true)
         (.addKeyListener panel))
      _ (.start (Timer. tik panel))
      _ (doto frame
             (.setContentPane panel)
             (.pack)
             (.setVisible true)
             (.setResizable false)
             (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE))]))
