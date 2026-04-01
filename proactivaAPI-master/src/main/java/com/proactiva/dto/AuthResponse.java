package com.proactiva.dto;

import com.proactiva.model.User;

/**
 * DTO para resposta de autenticação.
 * Envia exatamente o que o frontend espera.
 */
public class AuthResponse {

    private User user;
    private String message;
    private String token;

    public AuthResponse() {}

    public AuthResponse(User user, String message, String token) {
        this.user = user;
        this.message = message;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
