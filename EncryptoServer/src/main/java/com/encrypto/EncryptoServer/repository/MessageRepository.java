package com.encrypto.EncryptoServer.repository;

import com.encrypto.EncryptoServer.model.Messages;
import com.encrypto.EncryptoServer.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Messages, Long> {

    List<Messages> findBySenderAndReceiver(Users sender, Users receiver);
}
