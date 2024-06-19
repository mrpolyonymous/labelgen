/*
Copyright 2024 mrpolyonymous

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package mrpolyonymous.rebrickableapi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import mrpolyonymous.labelgenerator.Colour;
import mrpolyonymous.labelgenerator.Part;
import mrpolyonymous.labelgenerator.Utils;

/**
 * Code for communicating with the Rebrickable API at https://rebrickable.com/api/v3.
 */
public class ApiClient {

    private static final String API_BASE_URL = "https://rebrickable.com/api/v3/lego/";

    /** Rebrickable rate limits to one API call per second, this value is slightly longer than
     * a second just in case.  */
    private static final long MIN_MS_BETWEEN_CALLS = 1010;
    
    /** Biggest batch size when fetching multiple parts at once. This value chosen
     * to kepp URLs within most common URL length limits, just in case.  */
    public static final int PARTS_BATCH_SIZE = 75;
    
    /** HTTP OK code. This value must exist somewhere else but I couldn't find it. */
    private static final int HTTP_OK = 200;

    /** API key required to talk to Rebrickable API. See https://rebrickable.com/api/v3/docs. */
    private final String apiKey;

    /** When was the API last called, for rate limiting. */
    private long lastApiCallTime;
    
    /** HTTP communication object. It would be more convenient to use something like spring's RestTemplate
     * but it's overkill to bring in all of the required spring dependencies just for that.
     */
    private HttpClient httpClient;
    private ObjectMapper objectMapper;


    public ApiClient() {
        String apiKeyEnv = System.getenv("REBRICKABLE_API_KEY");
        Objects.requireNonNull(apiKeyEnv, "Environment variable REBRICKABLE_API_KEY must be set to a key obtained from https://rebrickable.com/users/<your_user_id>/settings/#api" );
        this.apiKey = apiKeyEnv;

        httpClient = HttpClient.newBuilder()
              .version(Version.HTTP_1_1)
              .followRedirects(Redirect.NEVER)
              .connectTimeout(Duration.ofSeconds(20))
              .build();
        
        objectMapper = new ObjectMapper();
    }
    
    private void rateLimit() {
        long now = System.currentTimeMillis();
        // Rebrickable API is rate-limited to about one call per second
        if (now - lastApiCallTime < MIN_MS_BETWEEN_CALLS) {
            try {
                Thread.sleep(MIN_MS_BETWEEN_CALLS - (now-lastApiCallTime));
                now = System.currentTimeMillis();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        lastApiCallTime = now;
    }
    
    private HttpRequest buildGetRequest(String uri) {
        // TODO - this assumes all IDs in URLs have correct encoding and don't require additional escaping
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofMinutes(2))
                .header("Accept", "application/json")
                .header("Authorization", "key " + apiKey)
                .GET()
                .build();
        return request;
    }

    private <T> T fetchApiObject(String uri, String errorMessage, Class<T> clazz)
            throws IOException, InterruptedException {
        rateLimit();
        HttpRequest request = buildGetRequest(uri);

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        lastApiCallTime = System.currentTimeMillis();
        if (response.statusCode() != HTTP_OK) {
            throw new NoSuchElementException(errorMessage + ", status code=" + response.statusCode());
        }

        T result = objectMapper.readValue(response.body(), clazz);
        return result;
    }

    public ApiPart fetchPart(Part part) throws IOException, InterruptedException {
        String uri = API_BASE_URL + "parts/ " + part.id() + "/";
        String errorMessage = "Failed to fetch part " + part;
        return fetchApiObject(uri, errorMessage, ApiPart.class);
    }
    
    public List<ApiPart> fetchParts(Collection<Part> parts) throws IOException, InterruptedException {
        List<List<Part>> batches = Utils.split(parts, PARTS_BATCH_SIZE);
        List<ApiPart> results = new ArrayList<>(parts.size());
        for (List<Part> batch : batches) {
            ApiParts apiParts = fetchPartsBatch(batch);
            results.addAll(apiParts.getResults());
        }
        return results;
    }

    private ApiParts fetchPartsBatch(List<Part> batch) throws IOException, InterruptedException {
        String uri = API_BASE_URL + "parts/?part_nums=";
        uri += batch.stream().map(part -> part.id()).collect(Collectors.joining("%2C"));
        String errorMessage = "Failed to fetch parts in batch";
        ApiParts parts = fetchApiObject(uri, errorMessage, ApiParts.class);
        if (parts.getCount() != batch.size()) {
            // TODO - add some way for caller to know about results mismatch
            System.err.println("Requested " + batch.size() + " parts from Rebrickable API, got " + parts.getCount());
        } else {
            System.out.println("Retrieved " + batch.size() + " parts from Rebrickable API");
        }
        return parts;
    }
    
    public ApiColour fetchColour(Colour colour) throws IOException, InterruptedException {
        String uri = API_BASE_URL + "colors/" + colour.id() + "/";
        String errorMessage = "Failed to fetch colour ID " + colour.id();
        return fetchApiObject(uri, errorMessage, ApiColour.class);
    }
    
    public ApiElement fetchElement(String elementId) throws IOException, InterruptedException {
        String uri = API_BASE_URL + "elements/" + elementId + "/";
        String errorMessage = "Failed to fetch element ID " + elementId;
        return fetchApiObject(uri, errorMessage, ApiElement.class);
    }
    
}
