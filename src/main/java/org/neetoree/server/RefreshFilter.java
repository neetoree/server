package org.neetoree.server;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexander <iamtakingiteasy> Tumin on 2016-12-12.
 */
public class RefreshFilter implements Filter {
    private final TokenStore tokenStore;
    private final AuthenticationManager authenticationManager;

    public RefreshFilter(TokenStore tokenStore, AuthenticationManager authenticationManager) {
        this.tokenStore = tokenStore;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        Map<String, String> map = getSingleValueMap(request);
        String clientId = map.get(OAuth2Utils.CLIENT_ID);
        try {
            String extracted = map.get("refresh_token");
            if (extracted != null) {
                OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(new DefaultOAuth2RefreshToken(extracted));
                if (authentication != null) {
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(clientId, "", Collections.singleton(new SimpleGrantedAuthority("USER")));
                    SecurityContextHolder.getContext().setAuthentication(token);
                }
            } else {
                String username = map.get("username");
                String password = map.get("password");
                if (username != null && password != null) {
                    WebAuthenticationDetailsSource source = new WebAuthenticationDetailsSource();
                    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
                    authRequest.setDetails(source.buildDetails(request));
                    Authentication authResult = authenticationManager.authenticate(authRequest);
                    SecurityContextHolder.getContext().setAuthentication(authResult);
                }
            }
        } finally {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }


    private Map<String, String> getSingleValueMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Map<String, String[]> parameters = request.getParameterMap();
        for (String key : parameters.keySet()) {
            String[] values = parameters.get(key);
            map.put(key, values != null && values.length > 0 ? values[0] : null);
        }
        return map;
    }


    @Override
    public void destroy() {

    }
}
