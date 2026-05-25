package com.ragin.bdd.cucumbertests.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findFirstByUserId(userId: String): Optional<UserEntity>
}
