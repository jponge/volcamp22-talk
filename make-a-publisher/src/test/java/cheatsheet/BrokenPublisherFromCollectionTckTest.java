package cheatsheet;

import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BrokenPublisherFromCollectionTckTest extends FlowPublisherVerification<String> {

    public BrokenPublisherFromCollectionTckTest() {
        super(new TestEnvironment());
    }

    @Override
    public Flow.Publisher<String> createFlowPublisher(long elements) {
        List<String> data = Stream.generate(() -> "yolo")
                .limit(elements)
                .collect(Collectors.toList());
        return new BrokenPublisherFromCollection<>(data);
    }

    @Override
    public Flow.Publisher<String> createFailedFlowPublisher() {
        return null;
    }
}
