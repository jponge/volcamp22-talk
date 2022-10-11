import misc.Helper;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RandomNumberPublisher implements Flow.Publisher<String> {

  private final long max;

  public RandomNumberPublisher(long max) {
    this.max = max;
  }

  @Override
  public void subscribe(Flow.Subscriber<? super String> subscriber) {
    subscriber.onSubscribe(new RandomNumberSubscription(subscriber));
  }

  class RandomNumberSubscription implements Flow.Subscription {

    private final Flow.Subscriber<? super String> subscriber;
    private final Random random = new Random();
    private long total = 0L;
    private volatile boolean cancelled;

    public RandomNumberSubscription(Flow.Subscriber<? super String> subscriber) {
      this.subscriber = subscriber;
    }

    private final AtomicLong demand = new AtomicLong();

    @Override
    public void request(long n) {
      if (cancelled) {
        return;
      }
      if (n <= 0) {
        cancel();
        subscriber.onError(new IllegalArgumentException("Bad demand request: " + n + " (non-positive)"));
        return;
      }
      Helper.add(demand, n);
      drainLoop();
    }

    private final AtomicInteger wip = new AtomicInteger();

    private void drainLoop() {
      if (wip.getAndIncrement() > 0) {
        return;
      }

      long outstandingDemand = demand.get();
      long emitted = 0L;

      while (true) {
        while (emitted < outstandingDemand && total < max && !cancelled) {
          var item = "[" + random.nextInt() + "]";
          subscriber.onNext(item);
          emitted++;
          total++;
        }
        if (cancelled) {
          return;
        }
        if (total == max) {
          cancel();
          subscriber.onComplete();
          return;
        }
        demand.addAndGet(-emitted);
        emitted = 0L;
        if (wip.decrementAndGet() == 0) {
          return;
        }
      }
    }

    @Override
    public void cancel() {
      cancelled = true;
    }
  }

  public static void main(String[] args) {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    RandomNumberPublisher publisher = new RandomNumberPublisher(20);
    publisher.subscribe(new Flow.Subscriber<>() {

      private int counter;

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
//                subscription.request(Long.MAX_VALUE);
        scheduler.scheduleAtFixedRate(
          () -> {
            subscription.request(2L);
            if (counter++ == 3) {
              System.out.println("Cancel");
              subscription.cancel();
            }
          },
          0, 500, TimeUnit.MILLISECONDS
        );
      }

      @Override
      public void onNext(String item) {
        System.out.println(">>> " + item);
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
      }

      @Override
      public void onComplete() {
        System.out.println("== DONE ==");
      }
    });
  }
}
