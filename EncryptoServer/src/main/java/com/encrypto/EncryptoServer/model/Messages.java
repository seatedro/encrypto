package com.encrypto.EncryptoServer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Users sender;

    @ManyToOne
    private Users receiver;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Instant timestamp;
}
