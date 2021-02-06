package com.ragin.bdd.cucumbertests.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

/**
 * Don't use such mixed JSON/database classes in a real-world object.
 * This example is only for testing.
 */
@Data
@Entity(name = "USER")
public class UserEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, length = 36, unique = true)
    @JsonIgnore
    Long id;
    @Column(name = "USER_NAME", nullable = false, length = 255, unique = true)
    @JsonProperty("userName")
    String userName;
    @Column(name = "USER_ID", nullable = false, length = 36, unique = true)
    @JsonProperty("userId")
    String userId;
}
