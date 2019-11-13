package com.sg.fsp.model;


import java.io.Serializable;


public class JWTResponse implements Serializable {
    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private String role;


    public JWTResponse(String jwttoken,String role) {
        this.jwttoken = jwttoken;
        this.role=role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }



    public String getToken() {
        return this.jwttoken;
    }
}
