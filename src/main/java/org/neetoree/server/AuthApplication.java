package org.neetoree.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexander <iamtakingiteasy> Tumin on 2016-12-09.
 */

@SpringBootApplication
@EnableConfigurationProperties
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTokenStore tokenStore(DataSource dataSource) {
        return new JdbcTokenStore(dataSource);
    }

    @Bean
    public DefaultTokenServices tokenServices(JdbcTokenStore tokenStore, UserService userService) {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore);
        defaultTokenServices.setAccessTokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(182));
        defaultTokenServices.setClientDetailsService(userService);
        defaultTokenServices.setRefreshTokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(365));
        defaultTokenServices.setSupportRefreshToken(true);
        defaultTokenServices.setReuseRefreshToken(false);
        return defaultTokenServices;
    }

    @EnableAuthorizationServer
    @Configuration
    public static class AuthorizationServerConfigurer extends AuthorizationServerConfigurerAdapter {
        private final AuthenticationManager authenticationManager;
        private final TokenStore tokenStore;
        private final UserService userService;
        private final DefaultTokenServices tokenServices;

        @Autowired
        public AuthorizationServerConfigurer(AuthenticationManager authenticationManager, TokenStore tokenStore, UserService userService, DefaultTokenServices tokenServices) {
            this.authenticationManager = authenticationManager;
            this.tokenStore = tokenStore;
            this.userService = userService;
            this.tokenServices = tokenServices;
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            security.addTokenEndpointAuthenticationFilter(new RefreshFilter(tokenStore, authenticationManager));
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.tokenStore(tokenStore)
                    .tokenServices(tokenServices)
                    .authenticationManager(authenticationManager);
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(userService);
        }
    }

    @EnableResourceServer
    @Configuration
    public static class SpringResourceServerConfigurer implements ResourceServerConfigurer {
        private final TokenStore tokenStore;
        private final AuthenticationManager authenticationManager;
        private final UserService userService;

        @Autowired
        public SpringResourceServerConfigurer(TokenStore tokenStore, AuthenticationManager authenticationManager, UserService userService) {
            this.tokenStore = tokenStore;
            this.authenticationManager = authenticationManager;
            this.userService = userService;
        }


        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.tokenStore(tokenStore);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().permitAll().and()
                    .userDetailsService(userService);
        }
    }
}
