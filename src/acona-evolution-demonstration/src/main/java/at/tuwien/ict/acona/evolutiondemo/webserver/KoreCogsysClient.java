package at.tuwien.ict.acona.evolutiondemo.webserver;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class KoreCogsysClient {

	private final static Logger log = LoggerFactory.getLogger(KoreCogsysClient.class);

	private String restUri = "";
	private final Client client = ClientBuilder.newClient();

	public KoreCogsysClient(String ipAddress) {
		this.restUri = ipAddress;
		// this.port = port;
		log.info("Started rest client for uri={}", this.restUri);
	}

	private Response createJsonRequest(String path, JsonObject payload) {
		Entity<String> entity = Entity.entity(payload.toString(), MediaType.APPLICATION_JSON);
		Response result = client
				.target(restUri).path(path)
				// .queryParam("q", this.cityName).queryParam("APPID" , this.userid)
				.request(MediaType.APPLICATION_JSON)
				.post(entity); // post(Entity.entity(emp, MediaType.APPLICATION_JSON));

		log.debug("Entity={}, result={}. Sent request path={}, payload={}", entity, result.toString(), path, payload);

		return result;
	}

	public String performRequest(String path, JsonObject payload) {
		log.info("Execute request. Address={}, payload={}", restUri + path, payload);
		Response resp = this.createJsonRequest(path, payload);

		if (resp.getStatus() > 299 && resp.getStatus() < 200) {
			throw new RuntimeException("Failed : HTTP error code : " + resp.getStatus() + ". Path= " + path + ". Payload=" + payload + ". Response=" + resp);
		}

		log.info("Received data={}", resp);

		String s = resp.readEntity(String.class);

		// Weather object = (new Gson()).fromJson(s, Weather.class);
		log.info("Got response= {}", s);
		return s;
	}

	// /**
	// * Execute the request to notify the director to start the simulator
	// *
	// * @param variantUri
	// */
	// public void performNewSolutionReady(JsonObject) {
	// JsonObject obj = new JsonObject();
	// obj.addProperty("UseCaseURI", useCaseUri);
	// obj.addProperty("scenarioURI", scenarioUri);
	// obj.addProperty("solutionURI", solutionUri);
	// obj.addProperty("VariantURI", variantUri);
	// this.performRequest("newSolution", obj);
	// }
	//
	// /**
	// * Tell the director to end the optimization
	// *
	// * @param scenarioUri
	// */
	// public void performEndOptimization(String useCaseUri, String scenarioUri, String solutionUri,
	// String variantUri, String simulationRunUri) {
	// JsonObject obj = new JsonObject();
	// obj.addProperty("UseCaseURI", useCaseUri);
	// obj.addProperty("scenarioURI", scenarioUri);
	// obj.addProperty("solutionURI", solutionUri);
	// obj.addProperty("VariantURI", variantUri);
	// obj.addProperty("simulationrunURI", simulationRunUri);
	// this.performRequest("endOptimization", obj);
	// }

	// Requests:
	// New solution ready
	// ContinueOptimization
	// EndOptimization

}
