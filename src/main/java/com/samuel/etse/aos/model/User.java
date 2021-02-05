package com.samuel.etse.aos.model;

import java.util.List;

import com.samuel.etse.aos.model.auth.BaseUser;

import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "App User", description = "An user in the app", parent = BaseUser.class)
@Document(collection = "users")
public class User extends BaseUser {

    @ApiModelProperty(name = "roles", value = "User roles", allowableValues = "ADMIN,MODERADOR,LECTOR,REDACTOR", example = "ADMIN", required = true)
    private List<String> roles;
    @ApiModelProperty(name = "subscriptions", value = "User´s subscriptions", example = "Diseño Web", required = false)
    private List<List<String>> suscripciones;

    public List<String> getRoles() {
        return roles;
    }

    public List<List<String>> getSuscripciones() {
        return suscripciones;
    }

    public User setSuscripciones(List<List<String>> suscripciones) {
        this.suscripciones = suscripciones;
        return this;
    }

    public User setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public User setPassword(String password) {
        super.setPassword(password);
        return this;
    }

    @Override
    public User setUsername(String username) {
        super.setUsername(username);
        return this;
    }

    @Override
    public User setActive(boolean isActive) {
        super.setActive(isActive);
        return this;
    }

}