package io.volcamp.reactive;

import io.smallrye.mutiny.Uni;
import io.volcamp.Fortune;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/fortune")
@RegisterRestClient(configKey = "fortunes-api")
public interface FortunesReactiveClient {

    @GET
    Uni<Response> fetch();

    default Uni<Fortune> fetchFortune() {
        return fetch().onItem().transform(response -> {
            var index = Integer.valueOf(response.getHeaderString("Fortune-Index"));
            var text = response.readEntity(String.class);
            return new Fortune(index, text);
        });
    }
}
