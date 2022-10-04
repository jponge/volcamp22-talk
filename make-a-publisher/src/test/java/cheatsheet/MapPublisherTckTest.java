package cheatsheet;

import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapPublisherTckTest extends FlowPublisherVerification<String> {

    public MapPublisherTckTest() {
        super(new TestEnvironment());
    }

    @Override
    public Flow.Publisher<String> createFlowPublisher(long elements) {
        List<String> data = Stream.generate(() -> "yolo")
                .limit(elements)
                .collect(Collectors.toList());
        CorrectPublisherFromCollection<String> source = new CorrectPublisherFromCollection<>(data);
        return new MapPublisher<>(source, String::toUpperCase);
    }

    @Override
    public Flow.Publisher<String> createFailedFlowPublisher() {
        return null;
    }

    @Override
    public long maxElementsFromPublisher() {
        return 4096L;
    }
}
