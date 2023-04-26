package org.example;

import java.io.IOException;
import java.util.List;


public class App {
    public static void main(String[] args) throws IOException {
        List<TokenWrapper> tokens = new Lexer().startLexer("example.go");
        for (TokenWrapper token : tokens) {
            System.out.println("[" + token.getToken() + " - " + token.getData() + "]");
        }
    }
}
