package org.example;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final List<TokenWrapper> tokens = new ArrayList<>();

    private Status currentStatus = Status.START;

    private StringBuilder statusBuffer = new StringBuilder();

    public List<TokenWrapper> startLexer(String filename) throws IOException {
        File file = new File(filename);
        InputStream inputStream = new FileInputStream(file);
        Reader reader = new InputStreamReader(inputStream, Charset.defaultCharset());
        Reader bufferedReader = new BufferedReader(reader);

        int symbol;
        while ((symbol = bufferedReader.read()) != -1) {
            Character c = (char) symbol;
            processSymbol(c);
        }

        processSymbol('\n');
        if (currentStatus != Status.START) {
            createToken(Token.ERROR);
        }

        return tokens;
    }

    private void processSymbol(Character c) {
        statusBuffer.append(c);

        switch (currentStatus) {
            case START -> startStatus(c);
            case ERROR -> errorStatus(c);
            case TEXT_PROCESSING -> textProcessingStatus(c);
            case QUO -> qouStatus(c);
            case DIGIT -> digitStatus(c);
            case CHAR -> charStatus(c);
            case STRING -> stringStatus(c);
            case PERIOD -> periodStatus(c);
            case GREATER -> greaterStatus(c);
            case LESS -> lessStatus(c);
            case AND -> andStatus(c);
            case OR -> orStatus(c);
            case OPERATOR -> operatorStatus(c);
            case COLON -> colonStatus(c);
            case ADD -> addStatus(c);
            case SUB -> subStatus(c);
            case SINGLE_LINE_COMMENT -> singleLineCommentStatus(c);
            case BLOCK_COMMENT -> blockCommentStatus(c);
            case PERIOD_IN_DIGIT -> periodInDigitStatus(c);
            case FLOAT -> floatStatus(c);
            case COMPLEX_NUMBER -> complexStatus(c);
            case INVALID_NUMBER -> invalidFloatStatus(c);
            case CHAR_PROCESSING -> charProcessingStatus(c);
            case SH -> shStatus(c);
            case OPERATOR_AND_EQUAL -> operatorAndEqualStatus(c);
            case CLOSING_BLOCK_COMMENT -> closeBlockCommentStatus(c);
            default -> System.out.println("Couldn't recognize state");

        }
    }


    private void startStatus(Character c) {
        if (c == '/') {
            currentStatus = Status.QUO;
        } else if (Character.isWhitespace(c) || c == '\n') {
            createToken(Token.WHITESPACE);
            currentStatus = Status.START;
        } else if (Character.isDigit(c)) {
            currentStatus = Status.DIGIT;
        } else if (c == '\'') {
            currentStatus = Status.CHAR;
        } else if (c == '\"') {
            currentStatus = Status.STRING;
        } else if (c == '.') {
            currentStatus = Status.PERIOD;
        } else if (Utils.isSeparator(c)) {
            createToken(Token.SEPARATOR);
            currentStatus = Status.START;
        } else if (c == '>') {
            currentStatus = Status.GREATER;
        } else if (c == '<') {
            currentStatus = Status.LESS;
        } else if (c == '&') {
            currentStatus = Status.AND;
        } else if (c == '^' || c == '!' || c == '*' || c == '=' || c == '%') {
            currentStatus = Status.OPERATOR;
        } else if (c == ':') {
            currentStatus = Status.COLON;
        } else if (c == '+') {
            currentStatus = Status.ADD;
        } else if (c == '-') {
            currentStatus = Status.SUB;
        } else if (c == '|') {
            currentStatus = Status.OR;
        } else if (Character.isAlphabetic(c) || c == '_') {
            currentStatus = Status.TEXT_PROCESSING;
        } else {
            currentStatus = Status.ERROR;
        }
    }

    private void createToken(Token token) {
        if (token == Token.WHITESPACE) {
            tokens.add(new TokenWrapper(token, " "));
        } else {
            tokens.add(new TokenWrapper(token, statusBuffer.toString()));
        }
        clearBuffer(null);
    }

    private void clearBuffer(Character firstNewChar) {
        statusBuffer = new StringBuilder();
        if (firstNewChar != null) {
            statusBuffer.append(firstNewChar);
        }
    }

    private void errorStatus(Character c) {
        addCompleteToken(Token.ERROR);
        currentStatus = Status.START;
        startStatus(c);
    }

    private void textProcessingStatus(Character c) {
        if (c == ' ' || Character.isWhitespace(c) || c == '\n' || Utils.isSeparator(c) || Utils.isOperator(c)) {
            if (Utils.isKeyword(statusBuffer.substring(0, statusBuffer.length() - 1))) {
                addCompleteToken(Token.KEYWORD);
            } else if (Utils.isBoolean(statusBuffer.substring(0, statusBuffer.length() - 1))) {
                addCompleteToken(Token.BOOLEAN);
            } else if (Utils.isNull(statusBuffer.substring(0, statusBuffer.length() - 1))) {
                addCompleteToken(Token.NULL);
            } else {
                addCompleteToken(Token.NAME);
            }
            currentStatus = Status.START;
            startStatus(c);
        } else if (!(Character.isLetter(c) || Character.isDigit(c) || c == '_')) {
            currentStatus = Status.ERROR;
        }
    }

    private void addCompleteToken(Token token) {
        String substring = statusBuffer.substring(0, statusBuffer.length() - 1);
        Character lastChar = statusBuffer.charAt(statusBuffer.length() - 1);
        tokens.add(new TokenWrapper(token, substring));
        clearBuffer(lastChar);
    }

    private void qouStatus(Character c) {
        if (c == '/') {
            currentStatus = Status.SINGLE_LINE_COMMENT;
        } else if (c == '*') {
            currentStatus = Status.BLOCK_COMMENT;
        } else if (c == '=') {
            currentStatus = Status.OPERATOR_AND_EQUAL;
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        } else {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void digitStatus(Character c) {
        if (c == '.') {
            currentStatus = Status.PERIOD_IN_DIGIT;
        } else if (c == 'i') {
            currentStatus = Status.COMPLEX_NUMBER;
        } else if (!Character.isDigit(c)) {
            if (Character.isWhitespace(c) || c == '\n' || Utils.isSeparator(c)) {
                addCompleteToken(Token.INT);
                currentStatus = Status.START;
                startStatus(c);
            } else if (!Character.isDigit(c)) {
                currentStatus = Status.INVALID_NUMBER;
            }
        }
    }

    private void charStatus(Character c) {
        if (Character.isWhitespace(c) || c == '\n') {
            addCompleteToken(Token.ERROR);
            currentStatus = Status.START;
            startStatus(c);
        } else {
            currentStatus = Status.CHAR_PROCESSING;
        }
    }

    private void stringStatus(Character c) {
        if (c == '\"') {
            if (statusBuffer.charAt(statusBuffer.length() - 2) != '\\') {
                createToken(Token.STRING);
                currentStatus = Status.START;
            } else {
                statusBuffer.deleteCharAt(statusBuffer.length() - 2);
            }
        }
    }

    private void periodStatus(Character c) {
        if (Character.isDigit(c)) {
            currentStatus = Status.PERIOD_IN_DIGIT;
        } else {
            addCompleteToken(Token.SEPARATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void greaterStatus(Character c) {
        if (c == '=') {
            createToken(Token.OPERATOR);
            currentStatus = Status.START;
        } else if (c == '>') {
            currentStatus = Status.SH;
        } else if (Utils.isOperator(c)) {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void lessStatus(Character c) {
        if (c == '=' || c == '<' || c == '-') {
            createToken(Token.OPERATOR);
            currentStatus = Status.START;
        } else if (c == '>') {
            currentStatus = Status.SH;
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        } else {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void andStatus(Character c) {
        if (c == '&') {
            createToken(Token.OPERATOR);
            currentStatus = Status.START;
        } else if (c == '=') {
            currentStatus = Status.OPERATOR_AND_EQUAL;
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        }
    }

    private void orStatus(Character c) {
        if (c == '|') {
            createToken(Token.OPERATOR);
            currentStatus = Status.START;
        } else if (c == '=') {
            currentStatus = Status.OPERATOR_AND_EQUAL;
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        } else {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void operatorStatus(Character c) {
        if (c == '=') {
            currentStatus = Status.OPERATOR_AND_EQUAL;
        } else if (c == '-' || c == '+') {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        } else {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void colonStatus(Character c) {
        if (c == '=') {
            createToken(Token.OPERATOR);
            currentStatus = Status.START;
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        } else {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void addStatus(Character c) {
        if (c == '+') {
            currentStatus = Status.OPERATOR;
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        } else {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void subStatus(Character c) {
        if (c == '-') {
            currentStatus = Status.OPERATOR;
        } else if (c == '=') {
            currentStatus = Status.OPERATOR_AND_EQUAL;
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        } else {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void singleLineCommentStatus(Character c) {
        if (c == '\n') {
            addCompleteToken(Token.COMMENT);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void blockCommentStatus(Character c) {
        if (c == '*') {
            currentStatus = Status.CLOSING_BLOCK_COMMENT;
        }
    }

    private void periodInDigitStatus(Character c) {
        if (Character.isDigit(c)) {
            currentStatus = Status.FLOAT;
        } else {
            currentStatus = Status.ERROR;
        }
    }

    private void floatStatus(Character c) {
        if (Character.isWhitespace(c) || c == '\n' || Utils.isSeparator(c)) {
            addCompleteToken(Token.FLOAT);
            currentStatus = Status.START;
            startStatus(c);
        } else if (c == 'i') {
            currentStatus = Status.COMPLEX_NUMBER;
        } else if (!Character.isDigit(c)) {
            currentStatus = Status.INVALID_NUMBER;
        }
    }

    private void complexStatus(Character c) {
        if (Character.isWhitespace(c) || c == '\n' || Utils.isSeparator(c)) {
            addCompleteToken(Token.COMPLEX);
            currentStatus = Status.START;
            startStatus(c);
        } else {
            currentStatus = Status.INVALID_NUMBER;
        }
    }

    private void invalidFloatStatus(Character c) {
        if (Character.isWhitespace(c) || c == '\n') {
            addCompleteToken(Token.ERROR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void charProcessingStatus(Character c) {
        if (c == '\'') {
            createToken(Token.CHAR);
            currentStatus = Status.START;
        } else {
            currentStatus = Status.ERROR;
        }
    }

    private void shStatus(Character c) {
        if (c == '=') {
            createToken(Token.OPERATOR);
            currentStatus = Status.START;
        } else if (c == '-' || c == '+') {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        } else {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }


    private void operatorAndEqualStatus(Character c) {
        if (c == '-' || c == '+') {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        } else if (Utils.isOperator(c)) {
            currentStatus = Status.ERROR;
        } else {
            addCompleteToken(Token.OPERATOR);
            currentStatus = Status.START;
            startStatus(c);
        }
    }

    private void closeBlockCommentStatus(Character c) {
        if (c == '/') {
            createToken(Token.COMMENT);
            currentStatus = Status.START;
        } else {
            currentStatus = Status.BLOCK_COMMENT;
        }
    }


}
