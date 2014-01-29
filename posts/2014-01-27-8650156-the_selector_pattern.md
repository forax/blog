
#### Fun with functional Programming in Java

Java 8 introduces functional interface (function type) and lambda (anonymous function) to Java making Java effectively a kind of functional language. But compared to Haskell or Clojure, Java is also an object oriented language so one cool thing is to try to mix the two.

Let supose I want to create an immutable linked list, in a typed language, you usually end up with 3 types, a type List which is an abstract type, a type Cons, a concrete type that contains an element and a pointer to the next List value and a type Nil which represent the end of the list.

Using a pseudo language, it's something like:

```
    List = Cons(E element, List next) | Nil
```

in Java, it will be:

```
interface List<E> {}
class Cons<E> implements List<E> { E element; List<E> next; }
class Nil<E> implements List<E> { }
```

and in that case, a list with the elements 1, 2, 3 will be created that way:

```
public static void main(String[] args) {
  List<Integer> list =
      new Cons<>(1, new Cons<>(2, new Cons<>(3, new Nil<>())));
}
```

Now, let's try to see how to write a functional version of the same code.
While the OO view is centered on the data, the functional view is centered on the algorithms,
so instead of trying to model the linked list, let's try to model how a linked list is used.

If by example we want to calculate the size of a linked list, again in a pseudo language:

```
size(List l) : int {
  return switch(l) {
    case Cons(element, next): 1 + size(next);
    case Nil:                 0; 
  }
}
```

i.e. if the list is a Cons, the size is 1 plus the size of the rest of the list, othewise if the list is a Nil, the size is 0.

In fact for all functions that will work on a List, we will have this de-composition, this pattern matching to know if the current element is a Cons or a Nil.
So if we try to abstract the concept of List as a high order function (a function that takes a function as parameter), a List is a function that takes two other functions.
This can be written that way:

```
V select(cons(E element, List next):V, nil():V)
```

a function select that takes a function cons that will take an element and the rest of the list and return a value and a function nil that take no parameter and return a value.
The function cons will be called is the List is a Cons, the function nil will be called if the list is a Nil.

BTW, this way of thinking is not new, 'if' in SmallTalk is implemented the same way.

In Java, it will be written like this:

```
interface List<E> {
  <V> V select(BiFunction<? super E, List<E>, ? extends V> cons,
               Supplier<? extends V> nil);
}
```

in that case Cons and Nil can be written that way:

```
public class Cons<E> implements List<E> { 
  E element; List<E> next;
  public <V> V select(BiFunction<? super E, List<E>, ? extends V> cons,
                      Supplier<? extends V> nil) {
    return cons.apply(element, next);
  }
}
public class Nil<E> implements List<E> {
  public <V> V select(BiFunction<? super E, List<E>, ? extends V> cons,
                      Supplier<? extends V> nil) {
    return nil.get();
  }
}
```

The implementation above is not great because the whole point of seeing the abstract type List as one abstract method is that it can be implemented by a lambda.
But the method select can not be implemented with a lambda because Java doesn't allow to implement generic method with a lambda (select is parameterized by V here). And it's not possible to use a method reference here too because while a method reference on a generic method is allowed a method reference can not capture several local variable values.

So practically, even if it's not beautiful, the best is to not return a value to avoid to parameterized the method, instead of select, we have a method ifThenElse that returns void.

```
interface List<E> {
  void ifThenElse(BiConsumer<? super E, List<E>, ? extends V> cons,
                  Runnable nil);
}
```

and with cons and nil two static methods to create a Cons or a Nil

```
interface List<E> {
  void ifThenElse(BiConsumer<? super E, List<E>, ? extends V> cons,
                  Runnable nil);

  public static <E> List<E> nil() {
    return (consumer, runnable) -> runnable.run();
  }
  
  public static <E> List<E> cons(E element, List<E> next) {
    Objects.requireNonNull(element);
    Objects.requireNonNull(next);
    return (consumer, runnable) -> consumer.accept(element, next);
  }
}
```
  
The implementation of nil() calls the runnable, and the implementation of cons() calls the consumer with the element and the rest of the list as parameter.
Creating a list is know simpler:

```
List<Integer> list =
      cons(1, cons(2, new cons(3, nil())));
```

In nil() the lambda doesn't capture a value from the enclosing scope, the JDK implementation will create a constant lambda thus two calls to nil() will return the same object.

```
public static void main(String[] args) {
  System.out.println(nil() == nil());  // true
}
```

which is exactly what we want (share one object nil for all lists).

Now, we can implement select using ifThenElse

```
@SuppressWarnings("unchecked")
public default <V> V select(BiFunction<? super E, List<E>, ? extends V> cons,
                            Supplier<? extends V> nil) {
  Object[] box = { null };
  ifThenElse(
      (element, next) -> box[0] = cons.apply(element, next),
      () -> box[0] = nil.get());
  return (V)box[0];
}
```

the side-effect makes the code a little dirty, but it is harmless (at least until someone create a new implementation of List).

With select, we can implement the method size:

```
public default int size() {
  return select((element, next) -> 1 + next.size(), () -> 0);
}
```

Or the method first, that return the first elements of the list:

```
public default List<E> first(int size) {
  if (size < 0) throw new IllegalArgumentException();
  return select(
      (element, next) -> size == 0? nil(): cons(element, next.first(size - 1)),
      List::nil);
}
```

and the methods map and flatMap:
[update: 29 jan 2014, thanks to Sebastian Millies that send me the correct code for flatMap]

```
public default <F> List<F> map(Function<? super E, ? extends F> mapper) {
  return select(
      (element, next) -> List.cons(mapper.apply(element), next.map(mapper)),
      List::nil);
}
  
public default <F> List<F> flatMap(BiFunction<? super E, List<F>, List<F>> mapper) {
  return select(
      (element, next) -> mapper.apply(element, next.flatMap(mapper)),
      List::nil);
} 
```

To summarize, Java 8 introduces the notion of functional interface, in order to type function which can be seen as a kind of hack to introduce the concept of function type and still keep the type system nominal.  This has the interesting consequence of bridging the gap between a curryfied function and an immutable class. Usually an immutable class has getters that publish its data while a curryfied function doesn't expose the values used when currying. If the abstract method of a functional interface is a high order function, it can publish the curryfied values. Moreover if the abstract method of a functional interface takes as parameter one functions by implementation of the abstract type, you get a nice encoding on any abstract data types.

I propose to name this pattern the Selector Pattern*. 

cheers,
Remi
*I don't think someone will ever use it but it's a nice way to finish this blog post.

