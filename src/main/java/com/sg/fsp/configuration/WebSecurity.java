package com.sg.fsp.configuration;


import com.sg.fsp.security.JWTAuthenticationEntryPoint;
import com.sg.fsp.security.JWTRequestFilter;
import com.sg.fsp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurity extends WebSecurityConfigurerAdapter {


    private JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private UserService jwtUserDetailsService;
    private JWTRequestFilter jwtRequestFilter;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public WebSecurity(JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint, UserService userService, JWTRequestFilter jwtRequestFilter, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.jwtAuthenticationEntryPoint=jwtAuthenticationEntryPoint;
        this.jwtUserDetailsService=userService;
        this.jwtRequestFilter=jwtRequestFilter;
        this.bCryptPasswordEncoder=bCryptPasswordEncoder;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }



    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF for this example
        httpSecurity.cors()
                .and()
                .csrf().disable()
                // dont authenticate this particular request
                .authorizeRequests().antMatchers("/api/authenticate").permitAll()
                .and()
                .authorizeRequests().antMatchers("/api/is-logged-in").permitAll()
                .and()
                .authorizeRequests().antMatchers("/api/signup/**").permitAll().
                and()
                .authorizeRequests().antMatchers("/api/signup/hello").permitAll().

        // all other requests need to be authenticated
                        anyRequest().authenticated().and().
                // make sure we use stateless session; session won't be used to
                // store user's state.
                        exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

}

