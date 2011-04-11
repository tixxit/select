/*
 * Copyright (c) 2011, Thomas Switzer
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package net.tixxit.select

import scala.annotation.tailrec


/**
 * Provides methods for linear-time selection/median finding.
 *
 * The method `select(a, k)` will select the `k`-th element from the array `a`.
 *
 * The method `quickSelect(a, k)` is similar, but does not guarantee linear
 * running time, but it is usually quicker in practice, taking about half as
 * much time.
 *
 * The method `mo5(a, i1, i2, i3, i4, i5)` returns the index of the median of
 * the 5 elements `{a(i1), a(i2), a(i3), a(i4), a(i5)}`.
 *
 * There are specialized versions of all methods for the types `Int`, `Float`
 * and `Double` that use their natural ordering and are significantly faster.
 *
 * @author [[mailto:thomas.switzer@gmail.com Tom Switzer]]
 */
object Selection {


  /**
	 * Select the `k`-th smallest element from the array `a`, where 0 <= `k`
	 * < `a.size`, and return it. The order of the elements in `a` after
	 * calling `select` is not guaranteed, other than that 
	 * `a(k) == select(a, k)`. The previous order will not be maintained.
	 *
	 * @param a An array to select from.
	 * @param k The magnitude (smallest-largest) of the element to select.
	 */
  def select[T](a: Array[T], k: Int)(implicit o: Ordering[T]): T = select(a, k, medianOfMedians[T], o.lt)

  /**
   * Select the `k`-th smallest element from the array `a`, where 0 <= `k`
   * < `a.size`, and return it. This version of `select` requires a custom
   *  ''less-than'' (`lt`) function that takes 2 parameters of type `T` and
   * returns `true` if the first parameter is less than the 2nd, and `false`
   * otherwise.
   *
   * @param a An array to select from.
   * @param k The magnitude (smallest-largest) of the element to select.
   * @param lt A ''less-than'' function.
   */
  def select[T](a: Array[T], k: Int, lt: (T, T) => Boolean): T = select(a, k, medianOfMedians[T], lt)

  def select(a: Array[Int], k: Int): Int = select(a, k, medianOfMedians: (Array[Int], Int, Int) => Int)
  def select(a: Array[Float], k: Int): Float = select(a, k, medianOfMedians: (Array[Float], Int, Int) => Int)
  def select(a: Array[Double], k: Int): Double = select(a, k, medianOfMedians: (Array[Double], Int, Int) => Int)

  
	/**
	 * Returns the `k`-th smallest element from the sequence `xs`, where 0
	 * <= `k` < `a.size`.
	 *
	 * @param xs The sequence to select from.
	 * @param k The magnitude (smallest-largest) of the element to select.
	 */
  def select[T](xs: Iterable[T], k: Int)(implicit o: Ordering[T], m: ClassManifest[T]): T = select(xs.toArray, k)
  def select[T](xs: Iterable[T], k: Int, lt: (T, T) => Boolean)(implicit m: ClassManifest[T]): T = select(xs.toArray, k, lt)
  def select(xs: Iterable[Int], k: Int)(implicit m: ClassManifest[Int]): Int = select(xs.toArray: Array[Int], k)
  def select(xs: Iterable[Float], k: Int)(implicit m: ClassManifest[Float]): Float = select(xs.toArray: Array[Float], k)
  def select(xs: Iterable[Double], k: Int)(implicit m: ClassManifest[Double]): Double = select(xs.toArray: Array[Double], k)


  /**
   * Select the `k`-th smallest element from the array `a`, where 0 <= `k`
   * < `a.size`, and return it. The method operates on the array in-place,
   * so the order of the elements in 'a' after calling `quickSelect` is not
   * guaranteed, other than that `quickSelect(a, k) == a(k)`. This operates
   * similar to `select`, but does not guarantee linear time complexity.
   *
   * @param a An array to select from.
   * @param k The magnitude (smallest-largest) of the element to select.
   */
  def quickSelect[T](a: Array[T], k: Int)(implicit o: Ordering[T]): T = select(a, k, quickMedian[T], o.lt)
  def quickSelect[T](a: Array[T], k: Int, lt: (T, T) => Boolean): T = select(a, k, quickMedian[T], lt)
  def quickSelect(a: Array[Int], k: Int): Int = select(a, k, quickMedian: (Array[Int], Int, Int) => Int)
  def quickSelect(a: Array[Float], k: Int): Float = select(a, k, quickMedian: (Array[Float], Int, Int) => Int)
  def quickSelect(a: Array[Double], k: Int): Double = select(a, k, quickMedian: (Array[Double], Int, Int) => Int)


  /**
   * Returns the `k`-th smallest element from the sequence `xs`, where 0
   * <= `k` < `a.size`. This is equivalent to running
   * `Selection.select(xs.toArray, k)`
   *
   * @param a An array to select from.
   * @param k The magnitude (smallest-largest of the element to select.
   */
  def quickSelect[T](xs: Iterable[T], k: Int)(implicit o: Ordering[T], m: ClassManifest[T]): T = quickSelect(xs.toArray, k)
  def quickSelect[T](xs: Iterable[T], k: Int, lt: (T, T) => Boolean)(implicit m: ClassManifest[T]): T = quickSelect(xs.toArray, k, lt)
  def quickSelect(xs: Iterable[Int], k: Int)(implicit m: ClassManifest[Int]): Int = quickSelect(xs.toArray, k)
  def quickSelect(xs: Iterable[Float], k: Int)(implicit m: ClassManifest[Float]): Float = quickSelect(xs.toArray, k)
  def quickSelect(xs: Iterable[Double], k: Int)(implicit m: ClassManifest[Double]): Double = quickSelect(xs.toArray, k)


  private def select[T](a: Array[T], k: Int, med: (Array[T], Int, Int, (T,T) => Boolean) => Int, lt: (T,T) => Boolean): T = select(a, 0, a.size, k, med, lt)

  private def select[T](a: Array[T], start: Int, end: Int, k: Int, med: (Array[T], Int, Int, (T,T) => Boolean) => Int, lt: (T,T) => Boolean): T = {
    def swap(i: Int, j: Int) = {
      val tmp = a(i)
      a(i) = a(j)
      a(j) = tmp
    }

    def split(start: Int, end: Int, p: Int): Int = {
      val pivot = a(p)
      var l = start
      var r = end - 1

      swap(p, l)
      do {
        swap(l, r)
        while (lt(a(l), pivot) || (!lt(pivot, a(l)) && l < p))
          l += 1
        while (r > l && (lt(pivot, a(r)) || (!lt(a(r), pivot) && r > p)))
          r -= 1
      } while (l < r)

      swap(l, end - 1)
      l
    }

    @tailrec
    def selectIn(l: Int, r: Int): T = {
      val m = split(l, r, med(a, l, r, lt))
      if (m < k)
        selectIn(m + 1, r)
      else if (m > k)
        selectIn(l, m)
      else
        a(k)
    }

    selectIn(start, end)
  }


  private def select(a: Array[Int], k: Int, med: (Array[Int], Int, Int) => Int): Int = select(a, 0, a.size, k, med)

  private def select(a: Array[Int], start: Int, end: Int, k: Int, med: (Array[Int], Int, Int) => Int): Int = {
    def swap(i: Int, j: Int) = {
      val tmp = a(i)
      a(i) = a(j)
      a(j) = tmp
    }

    def split(start: Int, end: Int, p: Int): Int = {
      val pivot = a(p)
      var l = start
      var r = end - 1

      swap(p, l)
      do {
        swap(l, r)
        while (a(l) < pivot || ((a(l) == pivot) && l < p))
          l += 1
        while (r > l && (a(r) > pivot || ((a(r) == pivot) && r > p)))
          r -= 1
      } while (l < r)

      swap(l, end - 1)
      l
    }

    @tailrec
    def selectIn(l: Int, r: Int): Int = {
      val m = split(l, r, med(a, l, r))
      if (m < k)
        selectIn(m + 1, r)
      else if (m > k)
        selectIn(l, m)
      else
        a(k)
    }

    selectIn(start, end)
  }


  private def select(a: Array[Float], k: Int, med: (Array[Float], Int, Int) => Int): Float = select(a, 0, a.size, k, med)

  private def select(a: Array[Float], start: Int, end: Int, k: Int, med: (Array[Float], Int, Int) => Int): Float = {
    def swap(i: Int, j: Int) = {
      val tmp = a(i)
      a(i) = a(j)
      a(j) = tmp
    }

    def split(start: Int, end: Int, p: Int): Int = {
      val pivot = a(p)
      var l = start
      var r = end - 1

      swap(p, l)
      do {
        swap(l, r)
        while (a(l) < pivot || ((a(l) == pivot) && l < p))
          l += 1
        while (r > l && (a(r) > pivot || ((a(r) == pivot) && r > p)))
          r -= 1
      } while (l < r)

      swap(l, end - 1)
      l
    }

    @tailrec
    def selectIn(l: Int, r: Int): Float = {
      val m = split(l, r, med(a, l, r))
      if (m < k)
        selectIn(m + 1, r)
      else if (m > k)
        selectIn(l, m)
      else
        a(k)
    }

    selectIn(start, end)
  }


  private def select(a: Array[Double], k: Int, med: (Array[Double], Int, Int) => Int): Double = select(a, 0, a.size, k, med)

  private def select(a: Array[Double], start: Int, end: Int, k: Int, med: (Array[Double], Int, Int) => Int): Double = {
    def swap(i: Int, j: Int) = {
      val tmp = a(i)
      a(i) = a(j)
      a(j) = tmp
    }

    def split(start: Int, end: Int, p: Int): Int = {
      val pivot = a(p)
      var l = start
      var r = end - 1

      swap(p, l)
      do {
        swap(l, r)
        while (a(l) < pivot || ((a(l) == pivot) && l < p))
          l += 1
        while (r > l && (a(r) > pivot || ((a(r) == pivot) && r > p)))
          r -= 1
      } while (l < r)

      swap(l, end - 1)
      l
    }

    @tailrec
    def selectIn(l: Int, r: Int): Double = {
      val m = split(l, r, med(a, l, r))
      if (m < k)
        selectIn(m + 1, r)
      else if (m > k)
        selectIn(l, m)
      else
        a(k)
    }

    selectIn(start, end)
  }
  

  private def quickMedian[T](a: Array[T], l: Int, r: Int, lt: (T,T) => Boolean) = {
    val len = r - l
    if (len < 5) {
      l + (len >> 1)
    } else {
      val s = (len / 5)
      mo5(a, l, l + s, l + 2 * s, l + 3 * s, l + 4 * s, lt)
    }
  }

  private def quickMedian(a: Array[Int], l: Int, r: Int): Int = {
    val len = r - l
    if (len < 5) {
      l + (len >> 1)
    } else {
      val s = (len / 5)
      mo5(a, l, l + s, l + 2 * s, l + 3 * s, l + 4 * s)
    }
  }

  private def quickMedian(a: Array[Float], l: Int, r: Int): Int = {
    val len = r - l
    if (len < 5) {
      l + (len >> 1)
    } else {
      val s = (len / 5)
      mo5(a, l, l + s, l + 2 * s, l + 3 * s, l + 4 * s)
    }
  }

  private def quickMedian(a: Array[Double], l: Int, r: Int): Int = {
    val len = r - l
    if (len < 5) {
      l + (len >> 1)
    } else {
      val s = (len / 5)
      mo5(a, l, l + s, l + 2 * s, l + 3 * s, l + 4 * s)
    }
  }
	

  /**
   * Returns an approximate median of `a`, between `l` and `r`, but with the
   * guarantee that at least a constant fraction `(~3(r - l)/10)` of the slice
   * of the array lie on either side of the median.
   *
   * @param a An array we're finding the median in.
   * @param l The lowerbound (inclusive) index we're searching in.
   * @param r The upperbound (exclusive) index we're searching in.
   * @param lt A function that returns true if its 1st argument is less-than
   *           its 2nd, and false otherwise.
   * @return The index of the approximate median in `a`.
   */
  private def medianOfMedians[T](a: Array[T], l: Int, r: Int, lt: (T,T) => Boolean): Int = {
    var newr = medians(a, l, r, lt)
    if (newr > (l + 1)) {
      val k = (l + newr) / 2
      select(a, l, newr, k, medianOfMedians[T], lt)
      k
    } else {
      l
    }
  }


  private def medianOfMedians(a: Array[Int], l: Int, r: Int): Int = {
    var newr = medians(a, l, r)
    if (newr > (l + 1)) {
      val k = (l + newr) / 2
      select(a, l, newr, k, medianOfMedians: (Array[Int], Int, Int) => Int)
      k
    } else {
      l
    }
  }


  private def medianOfMedians(a: Array[Float], l: Int, r: Int): Int = {
    var newr = medians(a, l, r)
    if (newr > (l + 1)) {
      val k = (l + newr) / 2
      select(a, l, newr, k, medianOfMedians: (Array[Float], Int, Int) => Int)
      k
    } else {
      l
    }
  }


  private def medianOfMedians(a: Array[Double], l: Int, r: Int): Int = {
    var newr = medians(a, l, r)
    if (newr > (l + 1)) {
      val k = (l + newr) / 2
      select(a, l, newr, k, medianOfMedians: (Array[Double], Int, Int) => Int)
      k
    } else {
      l
    }
  }
  

  /**
   * Partitions `a` into sets of 5 elements, finds the median of each, then
   * moves the medians to the front of the array (starting at index `l`). This
   * returns the index of the first non-median in a after moving all the
   * medians to the front (all the medians are between `l` and the return value
   * of `medians`).
   *
   * This will only operate between `l` (inclusive) and `r` (exclusive) in the
   * array. The rest of the array will be ignored.
   *
   * @param a An array to find medians in.
   * @param l The lowerbound (inclusive) index to start with.
   * @param r The upperbound (exclusive) index to end at.
   * @param lt A function that returns `true` if its 1st argument is less than
   *           its 2nd argument, and false otherwise.
   * @return The index of first element after the medians, which were stored at
   *         the front.
   */
  private def medians[T](a: Array[T], l: Int, r: Int, lt: (T,T) => Boolean): Int = {
    var e = l
    for (i <- l until (r - 4) by 5) {
      val m = mo5(a, i, i + 1, i + 2, i + 3, i + 4, lt)
      val tmp = a(m)
      a(m) = a(e)
      a(e) = tmp
      e += 1
    }
    return e
  }


  private def medians(a: Array[Int], l: Int, r: Int): Int = {
    var e = l
    for (i <- l until (r - 4) by 5) {
      val m = mo5(a, i, i + 1, i + 2, i + 3, i + 4)
      val tmp = a(m)
      a(m) = a(e)
      a(e) = tmp
      e += 1
    }
    return e
  }


  private def medians(a: Array[Float], l: Int, r: Int): Int = {
    var e = l
    for (i <- l until (r - 4) by 5) {
      val m = mo5(a, i, i + 1, i + 2, i + 3, i + 4)
      val tmp = a(m)
      a(m) = a(e)
      a(e) = tmp
      e += 1
    }
    return e
  }


  private def medians(a: Array[Double], l: Int, r: Int): Int = {
    var e = l
    for (i <- l until (r - 4) by 5) {
      val m = mo5(a, i, i + 1, i + 2, i + 3, i + 4)
      val tmp = a(m)
      a(m) = a(e)
      a(e) = tmp
      e += 1
    }
    return e
  }

  
  /**
   * Returns the index of the median of the 5 elements of the `Array` `a` at
   * the given indices (`i1`, `i2`, `i3`, `i4`, and `i5`).
   *
   * @param a An `Array` that contains the elements.
   */
	def mo5[T](a: Array[T], i1: Int, i2: Int, i3: Int, i4: Int, i5: Int)(implicit o: Ordering[T]): Int = mo5(a, i1, i2, i3, i4, i5, o.lt(_: T, _: T))


  def mo5[T](a: Array[T], i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, lt: (T,T) => Boolean): Int = {

    // This is, by necessity, a little messy. The idea is simple though. We
    // first remove ("drop") an element we know is in the top 2. This takes
    // 3 comparisons. Next, we drop another element we know is in the top
    // 2 (the max of the 4 we are currently considering). This takes
    // another 2 comparisons. Now we are left w/ 3 elements, 2 of which are
    // already ordered from previous comparisons. This means we can ignore
    // the lesser of the 2 ordered elements, so we are actually dealing
    // with only 2 elements, the greater of which is in the 3rd position.
    // Thus, 1 more comparison is all that is needed to find the median and
    // so a total of 3+2+1=6 comparisons is all that is needed to find the
    // median.

    // Note: The comments always use <, but often times the proper relation
    // is <=.

    val ai1 = a(i1)
    val ai2 = a(i2)
    val ai3 = a(i3)
    val ai4 = a(i4)
    val ai5 = a(i5)

    if (lt(ai1, ai2)) {            // i1 < i2
      if (lt(ai3, ai4)) {          // i1 < i2, i3 < i4
        if (lt(ai2, ai4)) {        // Drop i4
          if (lt(ai3, ai5)) {      // i1 < i2, i3 < i5
            if (lt(ai2, ai5)) {    // Drop i5
              if (lt(ai2, ai3)) i3 else i2
            } else {               // Drop i2
              if (lt(ai1, ai5)) i5 else i1
            }
          } else {                 // i1 < i2, i5 < i3
            if (lt(ai2, ai3)) {    // Drop i3
              if (lt(ai2, ai5)) i5 else i2
            } else {               // Drop i2
              if (lt(ai1, ai3)) i3 else i1
            }
          }
        } else {                   // Drop i2
          if (lt(ai1, ai5)) {      // i1 < i5, i3 < i4
            if (lt(ai5, ai4)) {    // Drop i4
              if (lt(ai5, ai3)) i3 else i5
            } else {                // Drop i5
              if (lt(ai1, ai4)) i4 else i1
            }
          } else {                 // i5 < i1, i3 < i4
            if (lt(ai1, ai4)) {    // Drop i4
              if (lt(ai1, ai3)) i3 else i1
            } else {                // Drop i1
              if (lt(ai5, ai4)) i4 else i5
            }
          }
        }
      } else {                     // i1 < i2, i4 < i3
        if (lt(ai2, ai3)) {        // Drop i3
          if (lt(ai4, ai5)) {      // i1 < i2, i4 < i5
            if (lt(ai2, ai5)) {    // Drop i5
              if (lt(ai2, ai4)) i4 else i2
            } else {               // Drop i2
              if (lt(ai1, ai5)) i5 else i1
            }
          } else {                 // i1 < i2, i5 < i4
            if (lt(ai2, ai4)) {    // Drop i4
              if (lt(ai2, ai5)) i5 else i2
            } else {               // Drop i2
              if (lt(ai1, ai4)) i4 else i1
            }
          }
        } else {                   // Drop i2
          if (lt(ai1, ai5)) {      // i1 < i5, i4 < i3
            if (lt(ai5, ai3)) {    // Drop i3
              if (lt(ai5, ai4)) i4 else i5
            } else {                // Drop i5
              if (lt(ai1, ai3)) i3 else i1
            }
          } else {                 // i5 < i1, i4 < i3
            if (lt(ai1, ai3)) {    // Drop i3
              if (lt(ai1, ai4)) i4 else i1
            } else {               // Drop i1
              if (lt(ai5, ai3)) i3 else i5
            }
          }
        }
      }
    } else {                       // i2 < i1
      if (lt(ai3, ai4)) {          // i2 < i1, i3 < i4
        if (lt(ai1, ai4)) {        // Drop i4
          if (lt(ai3, ai5)) {      // i2 < i1, i3 < i5
            if (lt(ai1, ai5)) {    // Drop i5
              if (lt(ai1, ai3)) i3 else i1
            } else {               // Drop i1
              if (lt(ai2, ai5)) i5 else i2
            }
          } else {                 // i2 < i1, i5 < i3
            if (lt(ai1, ai3)) {    // Drop i3
              if (lt(ai1, ai5)) i5 else i1
            } else {               // Drop i1
              if (lt(ai2, ai3)) i3 else i2
            }
          }
        } else {                   // Drop i1
          if (lt(ai2, ai5)) {      // i2 < i5, i3 < i4
            if (lt(ai5, ai4)) {    // Drop i4
              if (lt(ai5, ai3)) i3 else i5
            } else {                // Drop i5
              if (lt(ai2, ai4)) i4 else i2
            }
          } else {                 // i5 < i2, i3 < i4
            if (lt(ai2, ai4)) {    // Drop i4
              if (lt(ai2, ai3)) i3 else i2
            } else {               // Drop i2
              if (lt(ai5, ai4)) i4 else i5
            }
          }
        }
      } else {                     // i2 < i1, i4 < i3
        if (lt(ai1, ai3)) {        // Drop i3
          if (lt(ai4, ai5)) {      // i2 < i1, i4 < i5
            if (lt(ai1, ai5)) {    // Drop i5
              if (lt(ai1, ai4)) i4 else i1
            } else {               // Drop i1
              if (lt(ai2, ai5)) i5 else i2
            }
          } else {                 // i2 < i1, i5 < i4
            if (lt(ai1, ai4)) {    // Drop i4
              if (lt(ai1, ai5)) i5 else i1
            } else {                // Drop i1
              if (lt(ai2, ai4)) i4 else i2
            }
          }
        } else {                   // Drop i1
          if (lt(ai2, ai5)) {      // i2 < i5, i4 < i3
            if (lt(ai5, ai3)) {    // Drop i3
              if (lt(ai5, ai4)) i4 else i5
            } else {                // Drop i5
              if (lt(ai2, ai3)) i3 else i2
            }
          } else {                 // i5 < i2, i4 < i3
            if (lt(ai2, ai3)) {    // Drop i3
              if (lt(ai2, ai4)) i4 else i2
            } else {               // Drop i2
              if (lt(ai5, ai3)) i3 else i5
            }
          }
        }
      }
    }
	}


  def mo5(a: Array[Int], i1: Int, i2: Int, i3: Int, i4: Int, i5: Int): Int = {
    val ai1 = a(i1)
    val ai2 = a(i2)
    val ai3 = a(i3)
    val ai4 = a(i4)
    val ai5 = a(i5)

    if (ai1 < ai2) {                // i1 < i2
      if (ai3 < ai4) {              // i1 < i2, i3 < i4
        if (ai2 < ai4) {            // Drop i4
          if (ai3 < ai5) {          // i1 < i2, i3 < i5
            if (ai2 < ai5) {        // Drop i5
              if (ai2 < ai3) i3 else i2
            } else {                // Drop i2
              if (ai1 < ai5) i5 else i1
            }
          } else {                  // i1 < i2, i5 < i3
            if (ai2 < ai3) {        // Drop i3
              if (ai2 < ai5) i5 else i2
            } else {                // Drop i2
              if (ai1 < ai3) i3 else i1
            }
          }
        } else {                    // Drop i2
          if (ai1 < ai5) {          // i1 < i5, i3 < i4
            if (ai5 < ai4) {        // Drop i4
              if (ai5 < ai3) i3 else i5
            } else {                // Drop i5
              if (ai1 < ai4) i4 else i1
            }
          } else {                  // i5 < i1, i3 < i4
            if (ai1 < ai4) {        // Drop i4
              if (ai1 < ai3) i3 else i1
            } else {                // Drop i1
              if (ai5 < ai4) i4 else i5
            }
          }
        }
      } else {                      // i1 < i2, i4 < i3
        if (ai2 < ai3) {            // Drop i3
          if (ai4 < ai5) {          // i1 < i2, i4 < i5
            if (ai2 < ai5) {        // Drop i5
              if (ai2 < ai4) i4 else i2
            } else {                // Drop i2
              if (ai1 < ai5) i5 else i1
            }
          } else {                  // i1 < i2, i5 < i4
            if (ai2 < ai4) {        // Drop i4
              if (ai2 < ai5) i5 else i2
            } else {                // Drop i2
              if (ai1 < ai4) i4 else i1
            }
          }
        } else {                    // Drop i2
          if (ai1 < ai5) {          // i1 < i5, i4 < i3
            if (ai5 < ai3) {        // Drop i3
              if (ai5 < ai4) i4 else i5
            } else {                // Drop i5
              if (ai1 < ai3) i3 else i1
            }
          } else {                  // i5 < i1, i4 < i3
            if (ai1 < ai3) {        // Drop i3
              if (ai1 < ai4) i4 else i1
            } else {                // Drop i1
              if (ai5 < ai3) i3 else i5
            }
          }
        }
      }
    } else {                        // i2 < i1
      if (ai3 < ai4) {              // i2 < i1, i3 < i4
        if (ai1 < ai4) {            // Drop i4
          if (ai3 < ai5) {          // i2 < i1, i3 < i5
            if (ai1 < ai5) {        // Drop i5
              if (ai1 < ai3) i3 else i1
            } else {                // Drop i1
              if (ai2 < ai5) i5 else i2
            }
          } else {                  // i2 < i1, i5 < i3
            if (ai1 < ai3) {        // Drop i3
              if (ai1 < ai5) i5 else i1
            } else {                // Drop i1
              if (ai2 < ai3) i3 else i2
            }
          }
        } else {                    // Drop i1
          if (ai2 < ai5) {          // i2 < i5, i3 < i4
            if (ai5 < ai4) {        // Drop i4
              if (ai5 < ai3) i3 else i5
            } else {                // Drop i5
              if (ai2 < ai4) i4 else i2
            }
          } else {                  // i5 < i2, i3 < i4
            if (ai2 < ai4) {        // Drop i4
              if (ai2 < ai3) i3 else i2
            } else {                // Drop i2
              if (ai5 < ai4) i4 else i5
            }
          }
        }
      } else {                      // i2 < i1, i4 < i3
        if (ai1 < ai3) {            // Drop i3
          if (ai4 < ai5) {          // i2 < i1, i4 < i5
            if (ai1 < ai5) {        // Drop i5
              if (ai1 < ai4) i4 else i1
            } else {                // Drop i1
              if (ai2 < ai5) i5 else i2
            }
          } else {                  // i2 < i1, i5 < i4
            if (ai1 < ai4) {        // Drop i4
              if (ai1 < ai5) i5 else i1
            } else {                // Drop i1
              if (ai2 < ai4) i4 else i2
            }
          }
        } else {                    // Drop i1
          if (ai2 < ai5) {          // i2 < i5, i4 < i3
            if (ai5 < ai3) {        // Drop i3
              if (ai5 < ai4) i4 else i5
            } else {                // Drop i5
              if (ai2 < ai3) i3 else i2
            }
          } else {                  // i5 < i2, i4 < i3
            if (ai2 < ai3) {        // Drop i3
              if (ai2 < ai4) i4 else i2
            } else {                // Drop i2
              if (ai5 < ai3) i3 else i5
            }
          }
        }
      }
    }
	}


  def mo5(a: Array[Float], i1: Int, i2: Int, i3: Int, i4: Int, i5: Int): Int = {
    val ai1 = a(i1)
    val ai2 = a(i2)
    val ai3 = a(i3)
    val ai4 = a(i4)
    val ai5 = a(i5)

    if (ai1 < ai2) {                // i1 < i2
      if (ai3 < ai4) {              // i1 < i2, i3 < i4
        if (ai2 < ai4) {            // Drop i4
          if (ai3 < ai5) {          // i1 < i2, i3 < i5
            if (ai2 < ai5) {        // Drop i5
              if (ai2 < ai3) i3 else i2
            } else {                // Drop i2
              if (ai1 < ai5) i5 else i1
            }
          } else {                  // i1 < i2, i5 < i3
            if (ai2 < ai3) {        // Drop i3
              if (ai2 < ai5) i5 else i2
            } else {                // Drop i2
              if (ai1 < ai3) i3 else i1
            }
          }
        } else {                    // Drop i2
          if (ai1 < ai5) {          // i1 < i5, i3 < i4
            if (ai5 < ai4) {        // Drop i4
              if (ai5 < ai3) i3 else i5
            } else {                // Drop i5
              if (ai1 < ai4) i4 else i1
            }
          } else {                  // i5 < i1, i3 < i4
            if (ai1 < ai4) {        // Drop i4
              if (ai1 < ai3) i3 else i1
            } else {                // Drop i1
              if (ai5 < ai4) i4 else i5
            }
          }
        }
      } else {                      // i1 < i2, i4 < i3
        if (ai2 < ai3) {            // Drop i3
          if (ai4 < ai5) {          // i1 < i2, i4 < i5
            if (ai2 < ai5) {        // Drop i5
              if (ai2 < ai4) i4 else i2
            } else {                // Drop i2
              if (ai1 < ai5) i5 else i1
            }
          } else {                  // i1 < i2, i5 < i4
            if (ai2 < ai4) {        // Drop i4
              if (ai2 < ai5) i5 else i2
            } else {                // Drop i2
              if (ai1 < ai4) i4 else i1
            }
          }
        } else {                    // Drop i2
          if (ai1 < ai5) {          // i1 < i5, i4 < i3
            if (ai5 < ai3) {        // Drop i3
              if (ai5 < ai4) i4 else i5
            } else {                // Drop i5
              if (ai1 < ai3) i3 else i1
            }
          } else {                  // i5 < i1, i4 < i3
            if (ai1 < ai3) {        // Drop i3
              if (ai1 < ai4) i4 else i1
            } else {                // Drop i1
              if (ai5 < ai3) i3 else i5
            }
          }
        }
      }
    } else {                        // i2 < i1
      if (ai3 < ai4) {              // i2 < i1, i3 < i4
        if (ai1 < ai4) {            // Drop i4
          if (ai3 < ai5) {          // i2 < i1, i3 < i5
            if (ai1 < ai5) {        // Drop i5
              if (ai1 < ai3) i3 else i1
            } else {                // Drop i1
              if (ai2 < ai5) i5 else i2
            }
          } else {                  // i2 < i1, i5 < i3
            if (ai1 < ai3) {        // Drop i3
              if (ai1 < ai5) i5 else i1
            } else {                // Drop i1
              if (ai2 < ai3) i3 else i2
            }
          }
        } else {                    // Drop i1
          if (ai2 < ai5) {          // i2 < i5, i3 < i4
            if (ai5 < ai4) {        // Drop i4
              if (ai5 < ai3) i3 else i5
            } else {                // Drop i5
              if (ai2 < ai4) i4 else i2
            }
          } else {                  // i5 < i2, i3 < i4
            if (ai2 < ai4) {        // Drop i4
              if (ai2 < ai3) i3 else i2
            } else {                // Drop i2
              if (ai5 < ai4) i4 else i5
            }
          }
        }
      } else {                      // i2 < i1, i4 < i3
        if (ai1 < ai3) {            // Drop i3
          if (ai4 < ai5) {          // i2 < i1, i4 < i5
            if (ai1 < ai5) {        // Drop i5
              if (ai1 < ai4) i4 else i1
            } else {                // Drop i1
              if (ai2 < ai5) i5 else i2
            }
          } else {                  // i2 < i1, i5 < i4
            if (ai1 < ai4) {        // Drop i4
              if (ai1 < ai5) i5 else i1
            } else {                // Drop i1
              if (ai2 < ai4) i4 else i2
            }
          }
        } else {                    // Drop i1
          if (ai2 < ai5) {          // i2 < i5, i4 < i3
            if (ai5 < ai3) {        // Drop i3
              if (ai5 < ai4) i4 else i5
            } else {                // Drop i5
              if (ai2 < ai3) i3 else i2
            }
          } else {                  // i5 < i2, i4 < i3
            if (ai2 < ai3) {        // Drop i3
              if (ai2 < ai4) i4 else i2
            } else {                // Drop i2
              if (ai5 < ai3) i3 else i5
            }
          }
        }
      }
    }
	}


  def mo5(a: Array[Double], i1: Int, i2: Int, i3: Int, i4: Int, i5: Int): Int = {
    val ai1 = a(i1)
    val ai2 = a(i2)
    val ai3 = a(i3)
    val ai4 = a(i4)
    val ai5 = a(i5)

    if (ai1 < ai2) {                // i1 < i2
      if (ai3 < ai4) {              // i1 < i2, i3 < i4
        if (ai2 < ai4) {            // Drop i4
          if (ai3 < ai5) {          // i1 < i2, i3 < i5
            if (ai2 < ai5) {        // Drop i5
              if (ai2 < ai3) i3 else i2
            } else {                // Drop i2
              if (ai1 < ai5) i5 else i1
            }
          } else {                  // i1 < i2, i5 < i3
            if (ai2 < ai3) {        // Drop i3
              if (ai2 < ai5) i5 else i2
            } else {                // Drop i2
              if (ai1 < ai3) i3 else i1
            }
          }
        } else {                    // Drop i2
          if (ai1 < ai5) {          // i1 < i5, i3 < i4
            if (ai5 < ai4) {        // Drop i4
              if (ai5 < ai3) i3 else i5
            } else {                // Drop i5
              if (ai1 < ai4) i4 else i1
            }
          } else {                  // i5 < i1, i3 < i4
            if (ai1 < ai4) {        // Drop i4
              if (ai1 < ai3) i3 else i1
            } else {                // Drop i1
              if (ai5 < ai4) i4 else i5
            }
          }
        }
      } else {                      // i1 < i2, i4 < i3
        if (ai2 < ai3) {            // Drop i3
          if (ai4 < ai5) {          // i1 < i2, i4 < i5
            if (ai2 < ai5) {        // Drop i5
              if (ai2 < ai4) i4 else i2
            } else {                // Drop i2
              if (ai1 < ai5) i5 else i1
            }
          } else {                  // i1 < i2, i5 < i4
            if (ai2 < ai4) {        // Drop i4
              if (ai2 < ai5) i5 else i2
            } else {                // Drop i2
              if (ai1 < ai4) i4 else i1
            }
          }
        } else {                    // Drop i2
          if (ai1 < ai5) {          // i1 < i5, i4 < i3
            if (ai5 < ai3) {        // Drop i3
              if (ai5 < ai4) i4 else i5
            } else {                // Drop i5
              if (ai1 < ai3) i3 else i1
            }
          } else {                  // i5 < i1, i4 < i3
            if (ai1 < ai3) {        // Drop i3
              if (ai1 < ai4) i4 else i1
            } else {                // Drop i1
              if (ai5 < ai3) i3 else i5
            }
          }
        }
      }
    } else {                        // i2 < i1
      if (ai3 < ai4) {              // i2 < i1, i3 < i4
        if (ai1 < ai4) {            // Drop i4
          if (ai3 < ai5) {          // i2 < i1, i3 < i5
            if (ai1 < ai5) {        // Drop i5
              if (ai1 < ai3) i3 else i1
            } else {                // Drop i1
              if (ai2 < ai5) i5 else i2
            }
          } else {                  // i2 < i1, i5 < i3
            if (ai1 < ai3) {        // Drop i3
              if (ai1 < ai5) i5 else i1
            } else {                // Drop i1
              if (ai2 < ai3) i3 else i2
            }
          }
        } else {                    // Drop i1
          if (ai2 < ai5) {          // i2 < i5, i3 < i4
            if (ai5 < ai4) {        // Drop i4
              if (ai5 < ai3) i3 else i5
            } else {                // Drop i5
              if (ai2 < ai4) i4 else i2
            }
          } else {                  // i5 < i2, i3 < i4
            if (ai2 < ai4) {        // Drop i4
              if (ai2 < ai3) i3 else i2
            } else {                // Drop i2
              if (ai5 < ai4) i4 else i5
            }
          }
        }
      } else {                      // i2 < i1, i4 < i3
        if (ai1 < ai3) {            // Drop i3
          if (ai4 < ai5) {          // i2 < i1, i4 < i5
            if (ai1 < ai5) {        // Drop i5
              if (ai1 < ai4) i4 else i1
            } else {                // Drop i1
              if (ai2 < ai5) i5 else i2
            }
          } else {                  // i2 < i1, i5 < i4
            if (ai1 < ai4) {        // Drop i4
              if (ai1 < ai5) i5 else i1
            } else {                // Drop i1
              if (ai2 < ai4) i4 else i2
            }
          }
        } else {                    // Drop i1
          if (ai2 < ai5) {          // i2 < i5, i4 < i3
            if (ai5 < ai3) {        // Drop i3
              if (ai5 < ai4) i4 else i5
            } else {                // Drop i5
              if (ai2 < ai3) i3 else i2
            }
          } else {                  // i5 < i2, i4 < i3
            if (ai2 < ai3) {        // Drop i3
              if (ai2 < ai4) i4 else i2
            } else {                // Drop i2
              if (ai5 < ai3) i3 else i5
            }
          }
        }
      }
    }
	}


  class SelectableIntArray(a: Array[Int]) {
    def select(k: Int) = Selection.select(a, k)
    def quickSelect(k: Int) = Selection.quickSelect(a, k)
  }

  class SelectableFloatArray(a: Array[Float]) {
    def select(k: Int) = Selection.select(a, k)
    def quickSelect(k: Int) = Selection.quickSelect(a, k)
  }

  class SelectableDoubleArray(a: Array[Double]) {
    def select(k: Int) = Selection.select(a, k)
    def quickSelect(k: Int) = Selection.quickSelect(a, k)
  }


  class SelectableArray[T](a: Array[T])(implicit m: ClassManifest[T]) {
		def select(k: Int)(implicit o: Ordering[T]) = Selection.select(a, k)
    def quickSelect(k: Int)(implicit o: Ordering[T]) = Selection.quickSelect(a, k)
	}


  class SelectableIterable[T](xs: Iterable[T])(implicit m: ClassManifest[T]) {
    def select(k: Int)(implicit o: Ordering[T]) = Selection.select(xs, k)
    def quickSelect(k: Int)(implicit o: Ordering[T]) = Selection.quickSelect(xs, k)
  }
}
