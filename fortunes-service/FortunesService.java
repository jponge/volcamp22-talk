///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.vertx:vertx-core:4.3.4
//DEPS io.netty:netty-all:4.1.82.Final

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

public class FortunesService {

  private static String[] fortunes;
  private static Random random = new Random();

  private static Vertx vertx = Vertx.vertx();

  public static void main(String... args) throws Throwable {
    System.out.println("🚀 Starting");

    // Fortunes stuff
    fortunes = Files.readString(Path.of("bmc-fortunes", "fortunes"), StandardCharsets.UTF_8).split("\\n%\\n");

    // Vert.x server
    vertx.createHttpServer()
      .requestHandler(FortunesService::handleRequest)
      .listen(3000)
      .onSuccess(ok -> System.out.println("✅ Ready"))
      .onFailure(Throwable::printStackTrace);
  }

  static void handleRequest(HttpServerRequest req) {

    int index = random.nextInt(fortunes.length);
    String fortune = fortunes[index];

    String threadName = Thread.currentThread().getName();
    System.out.println("[" + threadName + "] serving fortune #" + index);

    req.response()
      .putHeader("Content-Type", "text/plain")
      .putHeader("Fortune-Index", String.valueOf(index))
      .end(fortune);
  }
}
