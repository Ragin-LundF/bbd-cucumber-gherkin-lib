package com.ragin.bdd.cucumbertests.database

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator

/**
 * Don't use such mixed JSON/database classes in a real-world object.
 * This example is only for testing.
 */
@Entity(name = "BDD_USER")
@SequenceGenerator(name = "id_gen", sequenceName = "hibernate_sequence", allocationSize = 1, initialValue = 1)
class UserEntity {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_gen")
    @Column(name = "ID", nullable = false, length = 36, unique = true)
    var id: Long? = null

    @JsonProperty("userName")
    @Column(name = "USER_NAME", nullable = false, length = 255, unique = true)
    lateinit var userName: String

    @JsonProperty("userId")
    @Column(name = "USER_ID", nullable = false, length = 36, unique = true)
    lateinit var userId: String
}
