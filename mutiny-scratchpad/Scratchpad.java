///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.smallrye.reactive:mutiny:1.7.0

import java.io.IOException;
import java.time.Duration;

import io.smallrye.mutiny.Multi;

public class Scratchpad {

  public static void main(String... args) {

  }
}

//    var stream = Multi.createFrom().generator(() -> 0, (state, emitter) -> {
//      if (state < 100) {
//        emitter.emit(state);
//        return state + 1;
//      } else {
//        emitter.complete();
//        return 0;
//      }
//    });
//
//    stream
//        .onItem().transform(n -> "[" + n + "]")
//        .onFailure(IOException.class).retry().atMost(5)
//        .ifNoItem().after(Duration.ofSeconds(5)).fail()
//        .subscribe().with(
//            item -> System.out.println(">>> " + item),
//            Throwable::printStackTrace,
//            () -> System.out.println("Done"));
