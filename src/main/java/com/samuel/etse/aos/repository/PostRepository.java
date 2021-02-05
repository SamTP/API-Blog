package com.samuel.etse.aos.repository;

import com.samuel.etse.aos.model.Post;

import java.util.List;

import org.bson.types.ObjectId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, ObjectId> {

    boolean existsByIdentificador(long identificador);

    void deleteByIdentificador(long identificador);
    void deleteByAutor(String autor);

    Post findByIdentificador(long identificador);

    List<Post> findAll();

    Page<Post> findAllByAutor(String autor,Pageable pageable);

    Page<Post> findAll(Pageable pageable);

    Post save(Post entity);

    Post findTopByOrderByIdentificadorDesc();

}