package org.neetoree.server;

import org.neetoree.server.orm.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander <iamtakingiteasy> Tumin on 2016-12-09.
 */
@Service
public class UserService implements UserDetailsService, ClientDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("LAUNCHER_USER"));
        return new User(userEntity.getUsername(), userEntity.getPassword(), authorities);
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        UserEntity byUsername = userRepository.findByUsername(clientId);
        if (byUsername == null) {
            throw new NoSuchClientException(clientId);
        }

        BaseClientDetails details = new BaseClientDetails(clientId, "oauth2-resource", "read,write,trust", "password,refresh_token,implicit,authorization_code", "LAUNCHER_USER");

        details.setClientSecret(byUsername.getPassword());
        return details;
    }
}
