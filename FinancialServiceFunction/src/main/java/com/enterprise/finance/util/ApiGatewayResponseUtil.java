package com.enterprise.finance.util;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Utility class for creating standardized API Gateway responses.
 */
public class ApiGatewayResponseUtil {

    private static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * Creates a success response with the given status code and body.
     *
     * @param statusCode The HTTP status code
     * @param body The response body
     * @return API Gateway response event
     */
    public static APIGatewayProxyResponseEvent createSuccessResponse(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", CONTENT_TYPE_JSON);
        return createSuccessResponse(statusCode, body, headers);
    }

    /**
     * Creates a success response with the given status code, body, and headers.
     *
     * @param statusCode The HTTP status code
     * @param body The response body
     * @param headers The response headers
     * @return API Gateway response event
     */
    public static APIGatewayProxyResponseEvent createSuccessResponse(int statusCode, String body, Map<String, String> headers) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(body)
                .withHeaders(headers)
                .withIsBase64Encoded(false);
    }

    /**
     * Creates an error response with the given status code and error message.
     *
     * @param statusCode The HTTP status code
     * @param errorMessage The error message
     * @return API Gateway response event
     */
    public static APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String errorMessage) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", CONTENT_TYPE_JSON);
        
        String errorBody = String.format("{\"error\":\"%s\"}", errorMessage);
        
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(errorBody)
                .withHeaders(headers)
                .withIsBase64Encoded(false);
    }
} 