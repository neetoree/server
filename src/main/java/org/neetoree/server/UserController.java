package org.neetoree.server;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;
import org.neetoree.server.orm.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by Alexander <iamtakingiteasy> Tumin on 2016-12-18.
 */
@RequestMapping("user")
@Controller
public class UserController {
    private final TokenStore tokenStore;
    private final UserRepository userRepository;

    private ReCaptcha reCaptcha = ReCaptchaFactory.newReCaptcha(System.getProperty("re.pub"), System.getProperty("re.priv"), false);

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
    public boolean signup(@RequestBody UserSignupForm form, HttpServletRequest request) {
        String header = request.getHeader("X-Real-IP");
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(header, form.getChallenge(), form.getUresponse());
        if (reCaptchaResponse.isValid()) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(form.getUsername());
            userEntity.setPassword(form.getPassword());
            userEntity.setCreated(new Date());
            userRepository.save(userEntity);
            return true;
        }
        return false;
    }
}
