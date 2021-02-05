package com.samuel.etse.aos.config;

import org.springframework.http.HttpHeaders;

public interface AuthConstants {

    String TOKEN_PREFIX = "Bearer";
    String TOKEN_SECRET = "YGsqZG0rJkxuL1AsOjtbQjBDLDV1K25nTXd0cCNZYU9jWn00UWY2U1VqJ35RNHp7MmBdOkxOJn";
    Long TOKEN_DURATION = 3600000L;
    String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
    String ROLES_CLAIM = "Roles";

}