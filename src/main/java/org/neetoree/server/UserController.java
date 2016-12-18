package org.neetoree.server;

import org.hibernate.engine.jdbc.ReaderInputStream;
import org.neetoree.server.orm.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexander <iamtakingiteasy> Tumin on 2016-12-18.
 */
@RequestMapping("user")
@Controller
public class UserController {
    private final TokenStore tokenStore;
    private final UserRepository userRepository;

    @Value("recaptcha.pub")
    private String pubkey;

    @Value("recaptcha.priv")
    private String privkey;

    @Autowired
    public UserController(TokenStore tokenStore, UserRepository userRepository) {
        this.tokenStore = tokenStore;
        this.userRepository = userRepository;
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

    @RequestMapping(value = "signup", method = RequestMethod.POST)
    @ResponseBody
    public boolean signup(@RequestBody UserSignupForm form, HttpServletRequest request) throws IOException {
        String header = request.getHeader("X-Real-IP");
        if (form.getUsername() == null || form.getPassword() == null || form.getUresponse() == null) {
            return false;
        }

        if (form.getUsername().length() < 3 || form.getUsername().length() > 20 || !form.getUsername().matches("[a-zA-Z0-9]+")) {
            return false;
        }

        RestTemplate template = new RestTemplate();
        Map<String, String> map = new HashMap<>();
        map.put("secret", privkey);
        map.put("response", form.getUsername());
        map.put("remoteip", header);
        InputStream is = new ReaderInputStream(new StringReader(template.postForObject("https://www.google.com/recaptcha/api/siteverify", map, String.class)));
        JsonObject object = Json.createReader(is).readObject();
        if (!object.getBoolean("success")) {
            return false;
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(form.getUsername());
        userEntity.setPassword(form.getPassword());
        userEntity.setCreated(new Date());
        userRepository.save(userEntity);
        return true;
    }
}
