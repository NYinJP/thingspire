package com.thingspire.thingspire.user;

import com.thingspire.thingspire.security.jwt.CustomAuthorityUtils;
import com.thingspire.thingspire.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.transaction.Transactional;
import java.util.List;


@Service
@Transactional
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils authorityUtils;

    // 생성자 DI
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CustomAuthorityUtils authorityUtils)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityUtils = authorityUtils;
    }

    public User createUser(User user){
        List<String> roles = authorityUtils.createRoles(user.getEmail());
        user.setRoles(roles);
        return userRepository.save(user);
    }
}
