package cheatsheet;

import misc.Helper;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class CorrectGeneratorPublisher<T> implements Flow.Publisher<T> {

    private final T init;
    private final Function<T, T> generator;

    public CorrectGeneratorPublisher(T init, Function<T, T> generator) {
        this.init = init;
        this.generator = generator;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new MySubscription(subscriber));
    }

    class MySubscription implements Flow.Subscription {

        private final Flow.Subscriber<? super T> subscriber;
        private volatile boolean cancelled;
        private T current = init;

        public MySubscription(Flow.Subscriber<? super T> subscriber) {
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
                subscriber.onError(new IllegalArgumentException("Bad demand: " + n + " (non-positive)"));
            } else {
                Helper.add(demand, n);
                drain();
            }
        }

        private final AtomicInteger wip = new AtomicInteger();

        private void drain() {
            if (wip.getAndIncrement() > 0) {
                return;
            }
            long currentDemand = demand.get();
            long emitted = 0L;
            while (true) {
                while (emitted < currentDemand) {
                    if (cancelled) {
                        return;
                    }
                    current = generator.apply(current);
                    if (current == null) {
                        cancel();
                        subscriber.onComplete();
                        return;
                    } else {
                        subscriber.onNext(current);
                    }
                    emitted++;
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
        AtomicInteger counter = new AtomicInteger();
        Function<Integer, Integer> generator = (current) -> {
            int n = counter.getAndIncrement();
            if (n > 100) {
                return null;
            } else {
                return n;
            }
        };

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        new CorrectGeneratorPublisher<>(0, generator).subscribe(new Flow.Subscriber<>() {

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
