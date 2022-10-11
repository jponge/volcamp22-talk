package io.volcamp.reactive;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.volcamp.Fortune;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/reactive")
public class ReactiveAPI {

  private static final Logger logger = LoggerFactory.getLogger(ReactiveAPI.class);

  @RestClient
  FortunesReactiveClient client;

  @GET
  public Uni<Fortune> fetchOne() {
    logger.info("Fetch one fortune (reactive)");
    return client.fetchFortune();
  }

//  @GET
//  @Path("/stream")
//  @Produces(MediaType.SERVER_SENT_EVENTS)
//  public Multi<Fortune> stream() {
//    logger.info("Starting streaming (reactive)");
//
//    return fetchOne().repeat().atMost(50)
//      .onItem().invoke(() -> logger.info("Sending event (reactive)"))
//      .onCompletion().invoke(() -> logger.info("Stopped streaming (reactive)"));
//  }
}
