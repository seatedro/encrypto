package com.encrypto.EncryptoServer.repository;

import com.encrypto.EncryptoServer.model.Messages;
import com.encrypto.EncryptoServer.model.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Messages, Long> {
    List<Messages> findBySenderAndReceiver(Users sender, Users receiver);

    @Query(
            "SELECT m FROM Messages m WHERE m.sender = :user OR m.receiver = :user ORDER BY m.timestamp ASC")
    Optional<List<Messages>> findAllBySenderOrReceiverId(@Param("user") Users user);
}
