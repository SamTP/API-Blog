package com.samuel.etse.aos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Se define la jerarquía de roles
 */
@Configuration
public class RolesConfig {
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
                "ROLE_ADMIN > ROLE_REDACTOR > ROLE_MOD > ROLE_LECTOR"
        );
        return hierarchy;
    }
}
