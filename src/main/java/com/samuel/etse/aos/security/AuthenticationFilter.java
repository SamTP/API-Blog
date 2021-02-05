package com.samuel.etse.aos.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.samuel.etse.aos.model.auth.BaseUser;
import com.samuel.etse.aos.service.UserAuthDetailsService;

import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import java.util.stream.Collectors;

import static com.samuel.etse.aos.config.AuthConstants.*;

/**
 * Se define el filtro de autenticacion de usuarios
 */
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager manager;

    // Servicio que nos devuelve los detalles de usuario con los roles
    private UserAuthDetailsService users;

    public AuthenticationFilter(UserAuthDetailsService users, AuthenticationManager manager) {
        this.manager = manager;
        this.users = users;
    }

    // Metodo que intenta la autenticación del usuario
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {

            // Leemos las credenciales de la peticion
            BaseUser credentials = new ObjectMapper().readValue(request.getInputStream(), BaseUser.class);

            // Intentamos autenticacion
            return manager.authenticate(new UsernamePasswordAuthenticationToken(credentials.getUsername(),
                    credentials.getPassword(), Collections.emptyList()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Si la autenticación es correcta se genera el JWT
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        Long now = System.currentTimeMillis();

        // Obtenemos los roles
        String authorities = authResult.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Construimos el token
        JwtBuilder tokenBuilder = Jwts.builder()
                // Establecemos el sujeto
                .setSubject(((User) (authResult.getPrincipal())).getUsername())
                // La fecha en la que fue emitido
                .setIssuedAt(new Date(now))
                // Fecha en la que caduca
                //.setExpiration(new Date(now + TOKEN_DURATION))
                // Los roles del usuario
                .claim(ROLES_CLAIM, authorities)
                // Lo firmamos con el token secreto
                .signWith(SignatureAlgorithm.HS512, TextCodec.BASE64.decode(TOKEN_SECRET))
                // Lo comprimimos
                .compressWith(CompressionCodecs.DEFLATE);

        // Añadimos el token a la cabecera de la respuesta con la clave AUTH_HEADER y el
        // prefijo "Bearer"
        response.addHeader(AUTH_HEADER, String.format("%s %s", TOKEN_PREFIX, tokenBuilder.compact()));
    }


}
