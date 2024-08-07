package br.com.pipocarosa.authentication;

import java.util.UUID;

public class AuthenticationResponse {

    private String jwt;
    private final UUID uuid;

    public AuthenticationResponse(String jwt, UUID uuid) {
        this.jwt = jwt;
        this.uuid = uuid;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public UUID getUuid() {
        return uuid;
    }
}
