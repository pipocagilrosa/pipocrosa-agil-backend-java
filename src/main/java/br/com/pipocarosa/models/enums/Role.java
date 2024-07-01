package br.com.pipocarosa.models.enums;

import java.util.Arrays;
import java.util.List;

public enum Role {

    USER(List.of(Permission.READ_PERSONAL_DATA)),

    ADMIN(Arrays.asList(Permission.READ_ALL_DATA, Permission.READ_PERSONAL_DATA));

    private List<Permission> permissions;

    Role(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}