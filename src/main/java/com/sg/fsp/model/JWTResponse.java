package com.sg.fsp.model;


import java.io.Serializable;


public class JWTResponse implements Serializable {
    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private User user;


    public JWTResponse(String jwttoken,User user) {
        this.jwttoken = jwttoken;
        this.user=user;
    }

    public String getToken() {
        return this.jwttoken;
    }
}
