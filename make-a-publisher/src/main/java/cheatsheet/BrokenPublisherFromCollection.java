package cheatsheet;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Flow;

public class BrokenPublisherFromCollection<T> implements Flow.Publisher<T> {

    private final List<T> elements;

    public BrokenPublisherFromCollection(List<T> elements) {
        this.elements = elements;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new MySubscription(subscriber));
    }

    private class MySubscription implements Flow.Subscription {

        private final Flow.Subscriber<? super T> subscriber;
        private final Iterator<T> iterator;
        private boolean cancelled;


        public MySubscription(Flow.Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
            iterator = elements.iterator();
        }

        @Override
        public void request(long n) {
            long counter = 0L;
            while (!cancelled && counter < n && iterator.hasNext()) {
                subscriber.onNext(iterator.next());
                counter++;
            }
            if (!cancelled && !iterator.hasNext()) {
                subscriber.onComplete();
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }

    public static void main(String[] args) {
        new BrokenPublisherFromCollection<>(List.of(1, 2, 3, 4, 5)).subscribe(new Flow.Subscriber<>() {

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
