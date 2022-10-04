package cheatsheet;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Function;

public class MapPublisher<I, O> implements Flow.Publisher<O> {

    private final Flow.Publisher<I> upstream;
    private final Function<I, O> function;

    public MapPublisher(Flow.Publisher<I> upstream, Function<I, O> function) {
        this.upstream = upstream;
        this.function = function;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super O> subscriber) {
        Mapper mapper = new Mapper();
        mapper.subscribe(subscriber);
    }

    class Mapper implements Flow.Processor<I, O>, Flow.Subscription {

        private Flow.Subscriber<? super O> subscriber;
        private Flow.Subscription subscription;

        @Override
        public void subscribe(Flow.Subscriber<? super O> subscriber) {
            this.subscriber = subscriber;
            upstream.subscribe(this);
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onNext(I item) {
            subscriber.onNext(function.apply(item));
        }

        @Override
        public void onError(Throwable throwable) {
            subscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            subscriber.onComplete();
        }

        @Override
        public void request(long n) {
            subscription.request(n);
        }

        @Override
        public void cancel() {
            subscription.cancel();
        }
    }

    public static void main(String[] args) {
        var source = new CorrectPublisherFromCollection<>(List.of(1, 2, 3, 4, 5));
        var map = new MapPublisher<>(source, n -> "[" + n + "]");

        map.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String item) {
                System.out.println(item);
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
