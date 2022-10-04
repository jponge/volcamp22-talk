package cheatsheet;

import misc.Helper;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CorrectPublisherFromCollection<T> implements Flow.Publisher<T> {

    private final List<T> elements;

    public CorrectPublisherFromCollection(List<T> elements) {
        this.elements = elements;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new MySubscription(subscriber));
    }

    class MySubscription implements Flow.Subscription {

        private final AtomicInteger wip = new AtomicInteger();
        private final AtomicLong demand = new AtomicLong();
        private final Flow.Subscriber<? super T> subscriber;
        private final Iterator<T> iterator;
        private volatile boolean cancelled;

        MySubscription(Flow.Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
            iterator = elements.iterator();
        }

        @Override
        public void request(long n) {
            if (n <= 0) {
                cancel();
                subscriber.onError(new IllegalArgumentException("Bad request of " + n + " elements (non-positive)"));
            } else {
                Helper.add(demand, n);
                drainLoop();
            }
        }

        private void drainLoop() {
            if (wip.getAndIncrement() > 0) {
                return;
            }
            long emitted = 0L;
            long pending = demand.get();
            while (true) {
                while (emitted < pending) {
                    if (cancelled) {
                        return;
                    }
                    if (!iterator.hasNext()) {
                        cancel();
                        subscriber.onComplete();
                        return;
                    }
                    T next = iterator.next();
                    if (next == null) {
                        cancel();
                        subscriber.onError(new NullPointerException("The list contains a null value"));
                        return;
                    }
                    subscriber.onNext(next);
                    if (!iterator.hasNext()) {
                        cancel();
                        subscriber.onComplete();
                        return;
                    }
                    emitted++;
                }
                pending = demand.addAndGet(-emitted);
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
        new CorrectPublisherFromCollection<>(List.of(1, 2, 3, 4, 5)).subscribe(new Flow.Subscriber<>() {

            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println(item);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("== done ==");
            }
        });
    }
}
