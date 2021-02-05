package com.samuel.etse.aos.model.auth;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "BaseUser", description = "User base details")
public class BaseUser {
    @ApiModelProperty(name = "id", value = "MongoDB Object ID", required = true)
    @Id
    private ObjectId id;
    @ApiModelProperty(name = "username", value = "The user name", example = "Samuel", required = true)
    private String username;
    @ApiModelProperty(name = "password", value = "The password", example = "123..", required = true)
    private String password;
    @ApiModelProperty(name = "email", value = "The user email", example = "mail@mail.com", required = true)
    private String email;
    @ApiModelProperty(name = "isActive", value = "User status in the system", example = "true", required = true)
    private boolean isActive;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return isActive;
    }

    public BaseUser setActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public BaseUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public BaseUser setPassword(String password) {
        this.password = password;
        return this;
    }

    public BaseUser setUsername(String username) {
        this.username = username;
        return this;
    }

}