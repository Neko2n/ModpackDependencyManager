package com.nekotune.mdm;

import com.nekotune.mdm.web.APITarget;
import com.nekotune.mdm.web.APITarget.APIResponse;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

public final class Server {

    private static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", 8080);

    public static void main(final String[] args) throws IOException {
        final HttpServer server = HttpServer.create(ADDRESS, 0);
        final HttpHandler requestHandler = new RequestHandler();
        final HttpContext context = server.createContext(
                "/", requestHandler);
        context.getFilters().add(new OpenAPIFilter());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server running on " + ADDRESS);
    }

    /**
     * @see OpenAPIFilter
     */
    private static final class RequestHandler implements HttpHandler {

        @Override
        public void handle(final HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed".getBytes());
                return;
            }

            // Parse the request path and project slug
            final String uriPath = exchange.getRequestURI().getPath();
            final String[] segments = uriPath.split("/");
            if (segments.length < 3) {
                sendResponse(exchange, 400, "Expected /{path}/{slug}".getBytes());
                return;
            }
            final String path = segments[1];
            final String slug = segments[2];

            // Read and validate headers
            final Headers headers = exchange.getRequestHeaders();
            final String resourceClass = headers.get("ResourceClass").get(0);
            final List<String> gameVersionsHeader = headers.get("GameVersions");
            if (gameVersionsHeader.isEmpty()) {
                sendResponse(exchange, 400, "Missing GameVersions header".getBytes());
                return;
            }
            final String[] gameVersions = gameVersionsHeader.get(0).split(",");

            // Fetch content from API target
            final APIResponse<byte[]> response;
            try {
                response = APITarget.GET(path, slug, resourceClass, gameVersions);
            } catch (IOException | InterruptedException e) {
                sendResponse(exchange, 500, "Server error".getBytes());
                return;
            }

            // Send auth failure responses
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                sendResponse(exchange, 502, "Upstream authentication error".getBytes());
                return;
            }
            if (response.statusCode() == 429) {
                sendResponse(exchange, 503, "Upstream rate limit exceeded".getBytes());
                return;
            }

            // Send successful response
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            sendResponse(exchange, response.statusCode(), response.body());
        }

        private void sendResponse(final HttpExchange exchange,
                final int statusCode, final byte[] body) throws IOException {
            exchange.sendResponseHeaders(statusCode, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }
    }
}
