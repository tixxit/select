package net.tixxit

package object select {
  implicit def array2selectableArray[A](a: Array[A])(implicit m: ClassManifest[A]): Selection.SelectableArray[A] = new Selection.SelectableArray(a)(m)

  implicit def array2selectableIntArray(a: Array[Int]): Selection.SelectableIntArray = new Selection.SelectableIntArray(a)
  implicit def array2selectableFloatArray(a: Array[Float]): Selection.SelectableFloatArray = new Selection.SelectableFloatArray(a)
  implicit def array2selectableDoubleArray(a: Array[Double]): Selection.SelectableDoubleArray = new Selection.SelectableDoubleArray(a)

  implicit def seq2selectableSeq[A](xs: Iterable[A])(implicit m: ClassManifest[A]): Selection.SelectableIterable[A] = new Selection.SelectableIterable(xs)
}

