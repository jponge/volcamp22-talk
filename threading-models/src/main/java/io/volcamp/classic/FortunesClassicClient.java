package io.volcamp.classic;

import io.volcamp.Fortune;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/fortune")
@RegisterRestClient(configKey = "fortunes-api")
public interface FortunesClassicClient {

  @GET
  Response fetch();

  default Fortune fetchFortune() {
    Response response = fetch();
    var index = Integer.valueOf(response.getHeaderString("Fortune-Index"));
    var text = response.readEntity(String.class);
    return new Fortune(index, text);
  }
}
