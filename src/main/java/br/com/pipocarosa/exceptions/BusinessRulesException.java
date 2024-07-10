package br.com.pipocarosa.exceptions;

public class BusinessRulesException extends RuntimeException {
    public BusinessRulesException() {
        super("Error in Business Rules");
    }
}
