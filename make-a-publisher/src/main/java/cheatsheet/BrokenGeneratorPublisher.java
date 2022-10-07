package cheatsheet;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BrokenGeneratorPublisher<T> implements Flow.Publisher<T> {

  private final T init;
  private final Function<T, T> generator;

  public BrokenGeneratorPublisher(T init, Function<T, T> generator) {
    this.init = init;
    this.generator = generator;
  }

  @Override
  public void subscribe(Flow.Subscriber<? super T> subscriber) {
    subscriber.onSubscribe(new MySubscription(subscriber));
  }

  class MySubscription implements Flow.Subscription {

    private final Flow.Subscriber<? super T> subscriber;
    private boolean cancelled;
    private T current = init;

    public MySubscription(Flow.Subscriber<? super T> subscriber) {
      this.subscriber = subscriber;
    }

    @Override
    public void request(long n) {
      long emitted = 0L;
      while (emitted < n && !cancelled) {
        current = generator.apply(current);
        if (current == null) {
          cancel();
          subscriber.onComplete();
        } else {
          subscriber.onNext(current);
        }
        emitted++;
      }
    }

    @Override
    public void cancel() {
      cancelled = true;
    }
  }

  public static void main(String[] args) {
    AtomicInteger counter = new AtomicInteger();
    Function<Integer, Integer> generator = (current) -> {
      int n = counter.getAndIncrement();
      if (n > 100) {
        return null;
      } else {
        return n;
      }
    };

//        new BrokenGeneratorPublisher<>(0, generator).subscribe(new Flow.Subscriber<>() {
//
//            private Flow.Subscription subscription;
//
//            @Override
//            public void onSubscribe(Flow.Subscription subscription) {
//                this.subscription = subscription;
//                subscription.request(1);
//            }
//
//            @Override
//            public void onNext(Integer item) {
//                System.out.println(item);
//                subscription.request(1);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                throwable.printStackTrace();
//            }
//
//            @Override
//            public void onComplete() {
//                System.out.println("== done ==");
//            }
//        });

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    new BrokenGeneratorPublisher<>(0, generator).subscribe(new Flow.Subscriber<>() {

      private ScheduledFuture<?> scheduledFuture;
      private Flow.Subscription subscription;

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> subscription.request(10L), 0, 1, TimeUnit.SECONDS);
      }

      @Override
      public void onNext(Integer item) {
        System.out.println(item);
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        scheduledFuture.cancel(true);
      }

      @Override
      public void onComplete() {
        System.out.println("== done ==");
        scheduledFuture.cancel(true);
      }
    });
  }
}
