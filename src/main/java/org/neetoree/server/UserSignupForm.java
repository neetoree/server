package org.neetoree.server;

/**
 * Created by Alexander <iamtakingiteasy> Tumin on 2016-12-18.
 */
public class UserSignupForm {
    private String username;
    private String password;
    private String challenge;
    private String uresponse;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getUresponse() {
        return uresponse;
    }

    public void setUresponse(String uresponse) {
        this.uresponse = uresponse;
    }
}
