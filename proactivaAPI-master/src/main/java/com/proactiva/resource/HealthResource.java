package com.proactiva.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Resource REST para verificação de saúde da API.
 * Endpoint: /api/health
 */
@Path("/api/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    /**
     * Verifica se a API está funcionando.
     * GET /api/health
     */
    @GET
    public Response health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Task Manager API está funcionando");
        response.put("timestamp", System.currentTimeMillis());
        
        return Response.ok(response).build();
    }
}
