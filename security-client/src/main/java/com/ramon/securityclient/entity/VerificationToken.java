package com.ramon.securityclient.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Data
@NoArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime expirationTime;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_USER_VERIFY_TOKEN")
    )
    private User user;

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expirationTime = calculateExpirationDate();
    }

    public VerificationToken(String token) {
        this.token = token;
        this.expirationTime = calculateExpirationDate();
    }

    //Time in minutes
    private static final int EXPIRATION_TIME = 10;
    private LocalDateTime calculateExpirationDate() {
        return LocalDateTime.now().plus(EXPIRATION_TIME, ChronoUnit.MINUTES);
    }
}
