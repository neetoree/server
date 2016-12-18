package org.neetoree.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Alexander <iamtakingiteasy> Tumin on 2016-12-18.
 */
@RequestMapping("user")
@Controller
public class UserController {
    private final TokenStore tokenStore;

    @Autowired
    public UserController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }


    @RequestMapping("check/{token}")
    @ResponseBody
    public boolean correct(@PathVariable String token) {
        OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
        if (oAuth2AccessToken == null) {
            return false;
        }
        return !oAuth2AccessToken.isExpired();
    }
}
