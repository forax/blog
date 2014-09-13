fun with lambda(s) ... 

### Java 8 - To the Infiny and Beyond

I'm really happy, my talk was accepted !
so i will talk at ...

```
import java.util.Random;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public interface SayHi {
  public static void main(String[] args) {
    IntFunction<String> fun = s -> new Random(s)
        .ints(6, 0, 27)
        .filter(v -> v != 0)
        .mapToObj(v -> "" + (char)('`' + v))
        .collect(Collectors.joining(""));
    
    System.out.println(fun.apply(-229985452));
    System.out.println(fun.apply(-2081939528));
  }
}

```

If you want to understand the output of this code, you can Google the first number -229985452 and obviously go to my talk (Monday, 10th November at 13:30 - 16:30).

long live to -1636202894 and see you there,
cheers,
Remi 

