package com.thingspire.thingspire.user;

import com.thingspire.thingspire.user.dto.UserDTO;
import com.thingspire.thingspire.user.mapper.UserMapper;
import com.thingspire.thingspire.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final MemberService memberService;
    private final UserMapper mapper;


    public UserController(MemberService memberService,
                          UserRepository userRepository,
                          UserMapper mapper) {
        this.memberService = memberService;
        this.mapper = mapper;
    }

//    @PostMapping("/users")
//    public ResponseEntity<User> createUser(@Valid @RequestBody CreatedUserDTO userDTO) throws URISyntaxException {
//        log.debug("REST request to save User : {}", userDTO);
//        userDTO.setEmail(userDTO.getEmail().toLowerCase());
//
//        User newUser = userService.createUser(userDTO);
//
//        return ResponseEntity.created(new URI("/api/admin/users/" + newUser.getLogin())).body(newUser);
//
//    }
    @PostMapping
    public ResponseEntity createUser(@Valid @RequestBody UserDTO.Post requestBody) {
        User newUser = memberService.createUser(mapper.userPostDTOToUser(requestBody));

        URI uri = UriComponentsBuilder.newInstance()
                .path("/api/users/" + newUser.getId())
                .build().toUri();

        return ResponseEntity.created(uri).build();
    }

}
