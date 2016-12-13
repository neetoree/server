package org.neetoree.server;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;

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
    private final TokenExtractor tokenExtractor = new BearerTokenExtractor();
    private final TokenStore tokenStore;

    public RefreshFilter(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
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
            Authentication extracted = tokenExtractor.extract(request);
            OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(new DefaultOAuth2RefreshToken(extracted.getPrincipal().toString()));
            if (authentication != null) {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(clientId, "", Collections.singleton(new SimpleGrantedAuthority("USER")));
                SecurityContextHolder.getContext().setAuthentication(token);
            }
        } finally {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }


    private Map<String, String> getSingleValueMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
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
