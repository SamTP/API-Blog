package com.samuel.etse.aos.security;

import java.util.Arrays;

import com.samuel.etse.aos.security.AuthenticationFilter;
import com.samuel.etse.aos.security.AuthorizationFilter;
import com.samuel.etse.aos.service.UserAuthDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración del módulo de autenticación
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AuthenticationConfig extends WebSecurityConfigurerAdapter {

    // Servicio que nos da los roles
    private final UserAuthDetailsService userDetailsService;

    // Encriptador de contrasenñas
    private final PasswordEncoder passwordEncoder;

    // Definicion de la jerarquía de roles
    private final RoleHierarchy roleHierarchy;

    // Inyectamos las dependencias
    @Autowired
    public AuthenticationConfig(UserAuthDetailsService userDetailsService, PasswordEncoder passwordEncoder,
            RoleHierarchy roleHierarchy) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.roleHierarchy = roleHierarchy;
    }

    // Configuramos los parámetros de seguridad
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // Se desactiva el filtro CSRF: Cross-Site request forgery
                .csrf().disable()
                // Se permiten todas las peticiones por defecto
                .authorizeRequests().anyRequest().permitAll().and()
                // Se definen los filtros de autorizacion y autenticacion
                .addFilter(new AuthorizationFilter(authenticationManager()))
                .addFilter(new AuthenticationFilter(userDetailsService, authenticationManager()))
                // Se desactiva el uso de cookies
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().cors();
        // http.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues());

    }

    // Se configura la clase que recupera los usuarios y el algoritmo de encriptado
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    // Configuramos la clase que provee la jerarquía de roles
    @Override
    public void configure(WebSecurity web) {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        web.expressionHandler(expressionHandler);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
