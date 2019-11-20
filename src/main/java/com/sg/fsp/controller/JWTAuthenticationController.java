package com.sg.fsp.controller;

import com.sg.fsp.model.JWTRequest;
import com.sg.fsp.model.JWTResponse;
import com.sg.fsp.model.User;
import com.sg.fsp.security.JWTTokenUtil;
import com.sg.fsp.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/*
Expose a POST API /authenticate using the JwtAuthenticationController.
The POST API gets the username and password in the body.
Using the Spring Authentication Manager, we authenticate the username and password.
If the credentials are valid, a JWT token is created using the JWTTokenUtil and is provided to the client.
 */

@RestController
@RequestMapping("/api")
@CrossOrigin
public class JWTAuthenticationController {

    private AuthenticationManager authenticationManager;
    private JWTTokenUtil jwtTokenUtil;
    private UserService userDetailsService;

    @Autowired
    public JWTAuthenticationController (AuthenticationManager authenticationManager, JWTTokenUtil jwtTokenUtil, UserService userService){
        this.jwtTokenUtil=jwtTokenUtil;
        this.authenticationManager=authenticationManager;
        this.userDetailsService=userService;
    }


    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JWTRequest authenticationRequest) {
        try {
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        }
        catch (Exception e){
            Map<String, String> res=new HashMap<>();
            res.put("message",e.getMessage());
            return new ResponseEntity<>(res,HttpStatus.UNAUTHORIZED);
        }
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        User user=userDetailsService.findByEmail(authenticationRequest.getUsername());
        if(user!=null&!user.isEnabled()){
            Map<String, String> res=new HashMap<>();
            res.put("message","Unauthorized/Access Denied");
            return new ResponseEntity<>(res,HttpStatus.UNAUTHORIZED);
        }
        User resUser=userDetailsService.findByEmail(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return new ResponseEntity<>(new JWTResponse(token,resUser),HttpStatus.OK);
    }


    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED");
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS");
        }
    }

    @RequestMapping(value = "/is-logged-in",method = RequestMethod.POST)
    public ResponseEntity authenticateToken(HttpServletResponse response, HttpServletRequest request) {
        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;
        Map<String, String> res=new HashMap<>();
        // JWT Token is in the form "Bearer token". Remove Bearer word and get
        // only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                res.put("message","Unable to get JWT Token");
                return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
            } catch (ExpiredJwtException e) {
                res.put("message","JWT Token has expired");
                return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
            }
        } else {
            res.put("message","JWT Token does not begin with Bearer String");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        if (username != null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                res.put("message","OK");
                return ResponseEntity.ok().body(res);
            }
            res.put("message","Unauthorized/Access Denied");
            return new ResponseEntity<>(res,HttpStatus.UNAUTHORIZED);
        }
        res.put("message","OK");
        return ResponseEntity.ok().body(res);
    }
}
