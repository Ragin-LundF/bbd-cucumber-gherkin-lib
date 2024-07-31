package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumbertests.UnknownUserException
import com.ragin.bdd.cucumbertests.database.UserEntity
import com.ragin.bdd.cucumbertests.database.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class DatabaseUser(
    private val userRepository: UserRepository
) {
    @GetMapping("/api/v1/user/db/{userId}")
    fun userDetails(@PathVariable("userId") userId: String): ResponseEntity<UserEntity> {
        val user = userRepository.findFirstByUserId(userId)

        if (user.isPresent) {
            return ResponseEntity.ok(user.get())
        } else {
            throw UnknownUserException("User not found")
        }
    }

    @PostMapping("/api/v1/user/db")
    fun storeUser(@RequestBody userEntity: UserEntity): ResponseEntity<UserEntity> {
        userEntity.userId = UUID.randomUUID().toString()
        return ResponseEntity.ok(userRepository.save(userEntity))
    }
}
