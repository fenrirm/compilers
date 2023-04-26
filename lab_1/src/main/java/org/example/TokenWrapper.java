package org.example;

public class TokenWrapper {
    private final Token token;
    private final String data;

    TokenWrapper(Token token, String data) {
        this.token = token;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public Token getToken() {
        return token;
    }
}
