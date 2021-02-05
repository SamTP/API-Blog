package com.samuel.etse.aos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.TextCodec;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.samuel.etse.aos.config.AuthConstants.*;

/**
 * Clase responsable de la autorizacion
 */
public class AuthorizationFilter extends BasicAuthenticationFilter {

    public AuthorizationFilter(AuthenticationManager manager){
        super(manager);
    }

    // Filtra la petición en busca del token
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try{

            // Se parsea la cabecera en busca del token de autorización
            String header = request.getHeader(AUTH_HEADER);

            // Si no está presente el token o no comienza con el prefijo adecuado se descarta la petición
            if(header == null || !header.startsWith(TOKEN_PREFIX)){
                chain.doFilter(request, response);
                return;
            }

            // Se comprueba el token
            UsernamePasswordAuthenticationToken authentication = getAuthentication(header);

            // Se establece la autenticación
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        } catch(ExpiredJwtException e){
            response.setStatus(419);
        }
    }

    // Comprueba el token
    private UsernamePasswordAuthenticationToken getAuthentication(String token) throws ExpiredJwtException {

        // Se parsea el token
        Claims claims = Jwts.parser()
                // Se establece la clave secreta con la que fue firmado
                .setSigningKey(TextCodec.BASE64.decode(TOKEN_SECRET))
                // Se obtiene el token en si
                .parseClaimsJws(token.replace(TOKEN_PREFIX, "").trim())
                // Se obtiene el cuerpo del token
                .getBody();

        // Se obtiene el usuario
        String user = claims.getSubject();

        // Se obtienen los roles
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get(ROLES_CLAIM).toString());

        return user == null ? null : new UsernamePasswordAuthenticationToken(user, token, authorities);
    }

}
