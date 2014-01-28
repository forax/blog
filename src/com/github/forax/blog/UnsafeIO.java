package com.github.forax.blog;

import java.io.IOError;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

class UnsafeIO {
  interface FunctionIO<U,R> { R apply(U u) throws IOException; }
  interface BiConsumerIO<U,V> { void accept(U u, V v) throws IOException; }
  
  static <U,R> Function<U,R> unsafeIO(FunctionIO<? super U, ? extends R> function) {
    return x -> {
      try {
        return function.apply(x);
      } catch (IOException e) {
        throw new IOError(e);
      }
    };
  }
  
  static <U,V,R> BiConsumer<U,V> unsafeIO(BiConsumerIO<? super U, ? super V> function) {
    return (x, y) -> {
      try {
        function.accept(x, y);
      } catch (IOException e) {
        throw new IOError(e);
      }
    };
  }
}
