import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;

import java.util.concurrent.Flow;

public class RandomNumberPublisherTckTest
  extends FlowPublisherVerification<String> {

  public RandomNumberPublisherTckTest() {
    super(new TestEnvironment());
  }

  @Override
  public Flow.Publisher<String> createFlowPublisher(long elements) {
    return new RandomNumberPublisher(elements);
  }

  @Override
  public Flow.Publisher<String> createFailedFlowPublisher() {
    return null;
  }
}
