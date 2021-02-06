package com.ragin.bdd.cucumbertests.library.test;

import com.ragin.bdd.cucumbertests.UnknownUserException;
import com.ragin.bdd.cucumbertests.database.UserEntity;
import com.ragin.bdd.cucumbertests.database.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DatabaseUser {
    private final UserRepository userRepository;

    @GetMapping("/api/v1/user/db/{userId}")
    public ResponseEntity<UserEntity> userDetails(@PathVariable("userId") final String userId) {
        final Optional<UserEntity> user = userRepository.findFirstByUserId(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            throw new UnknownUserException("User not found");
        }
    }

    @PostMapping("/api/v1/user/db")
    public ResponseEntity<UserEntity> storeUser(@RequestBody final UserEntity userEntity) {
        userEntity.setUserId(UUID.randomUUID().toString());
        return ResponseEntity.ok(userRepository.save(userEntity));
    }
}
