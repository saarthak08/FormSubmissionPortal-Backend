package com.sg.fsp.controller;

import com.sg.fsp.model.JWTRequest;
import com.sg.fsp.model.JWTResponse;
import com.sg.fsp.model.User;
import com.sg.fsp.security.JWTTokenUtil;
import com.sg.fsp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/*
Expose a POST API /authenticate using the JwtAuthenticationController.
The POST API gets the username and password in the body.
Using the Spring Authentication Manager, we authenticate the username and password.
If the credentials are valid, a JWT token is created using the JWTTokenUtil and is provided to the client.
 */

@RestController
@CrossOrigin
public class JWTAuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTTokenUtil jwtTokenUtil;
    @Autowired
    private UserService userDetailsService;


    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JWTRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        User user=userDetailsService.findByEmail(authenticationRequest.getUsername());
        if(user!=null&!user.isEnabled()){
            throw new ResponseStatusException( HttpStatus.UNAUTHORIZED,"User not enabled");
        }
        final String token = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JWTResponse(token));
    }


    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
