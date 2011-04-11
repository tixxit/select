Scala Selection Library
=======================

This library provides a linear-time selection library for Scala. This is all
bundled up in one nice class, net.tixxit.select.Selection. You can start
using it right away using:

	import net.tixxit.select._

The functionality is pretty straightfoward. To run through a few examples,

	import scala.util.Random
	
	val x = Random.shuffle(1 until 500000 toList) toArray

Let's select the 123456th element, so it returns 123457 from the array. It
will move 123457 to the 123456th position.

	assert(x.select(123456) == 123457)
	assert(x(123456) == 123457)

As you'll notice, selection from Arrays is in-place. However, selection from
Iterables, like lists and sets, is not. So, selecting from something that is
not an Array won't modify it. For instance,

	val y = Random.shuffle(1 until 100000)

	assert(y.select(123456) == 123457)
	assert(y(123456) != 123457)	// Probably... ;)

Specialization for Int, Float, and Double
-----------------------------------------

Another thing to keep in mind is that there are specialized versions for Int,
Float, and Double when using Arrays. These are not specialized in the "no
auto-boxing" sense (ie. @specialized), but rather they use the natural order
of these types and exploit the fact that Java's underlying type system keeps
Array's type parameters. That is, while List[Int] becomes just a List once
compiled (ie. type erasure), Array[Int] stays Array[Int]. This means makes
selection using Int, Float, or Double's natural order significantly faster
than the normal select.

However, sometimes you don't want to use the types natural orderings. You can
get around this by calling an unambiguous version of select.

	Selection.select(x, 123456, (_: Int) > (_: Int))

Another option is to just give the select method a type parameter.

	Selection.select[Int](x, 123456)

Thanks!
-------

And that's it. Let me know if you have any issues, comments, or questions
at thomas.switzer@gmail.com.
