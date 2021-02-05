package com.samuel.etse.aos.repository;

import com.samuel.etse.aos.model.User;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,ObjectId>{

    boolean existsByUsername(String username);
    void deleteByUsername(String username);
    User findByUsername(String username);
    List<User> findAll();
    Page<User> findAll(Pageable pageable);
    Page<User> findAllByRolesContains(String rol,Pageable pageable);
    User save(User entity);

}