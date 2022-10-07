package io.volcamp.classic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.volcamp.Fortune;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

@Path("/classic")
public class ClassicAPI {

  private static final Logger logger = LoggerFactory.getLogger(ClassicAPI.class);

  @RestClient
  FortunesClassicClient client;

  @GET
  public Fortune fetchOne() {
    logger.info("Fetch one fortune (classic)");
    return client.fetchFortune();
  }

  @GET
  @Path("/stream")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void stream(Sse sse, SseEventSink sink) throws JsonProcessingException {
    logger.info("Starting streaming (classic)");

    SseBroadcaster broadcaster = sse.newBroadcaster();
    broadcaster.register(sink);

    ObjectMapper jsonMapper = new ObjectMapper();
    for (int i = 0; i < 50; i++) {
      logger.info("Sending event (classic)");
      var fortune = client.fetchFortune();
      var json = jsonMapper.writeValueAsString(fortune);
      broadcaster.broadcast(sse.newEvent(json));
    }

    broadcaster.close();
    logger.info("Stopped streaming (classic)");
  }
}
