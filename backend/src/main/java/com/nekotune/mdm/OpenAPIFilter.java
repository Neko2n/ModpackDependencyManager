package com.nekotune.mdm;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

public class OpenAPIFilter extends Filter {

    private final OpenApiInteractionValidator validator;

    public OpenAPIFilter() {
        this.validator = OpenApiInteractionValidator
                .createFor("openapi.yaml")
                .build();
    }

    @Override
    public String description() {
        return "Validates HTTP requests against the OpenAPI spec";
    }

    @Override
    public void doFilter(final HttpExchange exchange, final Chain chain) throws IOException {
        final Request request = toValidatorRequest(exchange);
        final ValidationReport report = validator.validateRequest(request);
        if (report.hasErrors()) {
            final String message = report.getMessages().stream()
                    .map(ValidationReport.Message::getMessage)
                    .reduce("", (a, b) -> a + b + "\n");
            final byte[] body = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }
        chain.doFilter(exchange);
    }

    private Request toValidatorRequest(final HttpExchange exchange) throws IOException {
        final String method = exchange.getRequestMethod();
        final String path = exchange.getRequestURI().getPath();
        final String query = exchange.getRequestURI().getQuery();

        final SimpleRequest.Builder builder =
                new SimpleRequest.Builder(method, path);

        if (query != null) {
            for (final String param : query.split("&")) {
                final String[] kv = param.split("=", 2);
                builder.withQueryParam(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }

        // Copy headers across
        for (final Map.Entry<String, List<String>> header
                : exchange.getRequestHeaders().entrySet()) {
            for (final String value : header.getValue()) {
                builder.withHeader(header.getKey(), value);
            }
        }

        // Copy body, if present
        final byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
        if (bodyBytes.length > 0) {
            builder.withBody(bodyBytes);
        }

        return builder.build();
    }
}
