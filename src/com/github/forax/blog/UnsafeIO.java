package com.github.forax.blog;

import java.io.IOError;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class UnsafeIO {
  interface FunctionIO<U,R> { R apply(U u) throws IOException; }
  interface BiConsumerIO<U,V> { void accept(U u, V v) throws IOException; }
  interface ConsumerIO<U> { void accept(U u) throws IOException; }
  interface SupplierIO<R> { R get() throws IOException; }
  
  static <U,R> Function<U,R> unsafeFun(FunctionIO<? super U, ? extends R> function) {
    return x -> {
      try {
        return function.apply(x);
      } catch (IOException e) {
        throw new IOError(e);
      }
    };
  }
  
  static <U,V,R> BiConsumer<U,V> unsafeProc(BiConsumerIO<? super U, ? super V> function) {
    return (x, y) -> {
      try {
        function.accept(x, y);
      } catch (IOException e) {
        throw new IOError(e);
      }
    };
  }
  
  static <U> Consumer<U> unsafeProc(ConsumerIO<? super U> consumer) {
    return x -> {
      try {
        consumer.accept(x);
      } catch (IOException e) {
        throw new IOError(e);
      }
    };
  }
  
  static <R> Supplier<R> unsafeFun(SupplierIO<? extends R> supplier) {
    return () -> {
      try {
        return supplier.get();
      } catch (IOException e) {
        throw new IOError(e);
      }
    };
  }
}
