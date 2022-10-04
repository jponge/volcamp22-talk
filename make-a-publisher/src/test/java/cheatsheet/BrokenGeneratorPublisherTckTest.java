package cheatsheet;

import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;

import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class BrokenGeneratorPublisherTckTest extends FlowPublisherVerification<Long> {

    public BrokenGeneratorPublisherTckTest() {
        super(new TestEnvironment());
    }

    @Override
    public Flow.Publisher<Long> createFlowPublisher(long elements) {
        AtomicLong counter = new AtomicLong();
        Function<Long, Long> generator = (current) -> {
            long n = counter.getAndIncrement();
            if (n >= elements) {
                return null;
            } else {
                return n;
            }
        };
        return new BrokenGeneratorPublisher<>(0L, generator);
    }

    @Override
    public Flow.Publisher<Long> createFailedFlowPublisher() {
        return null;
    }
}
