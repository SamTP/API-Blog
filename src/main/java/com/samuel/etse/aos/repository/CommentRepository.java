package com.samuel.etse.aos.repository;

import com.samuel.etse.aos.model.Comment;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, ObjectId> {

    boolean existsByIdentificadorAndPost(long identificador,String post);
    boolean existsByPost(String post);

    void deleteByIdentificadorAndPost(long identificador,String post);

    void deleteByAutor(String autor);
    void deleteByPost(String post);

    Comment findByIdentificadorAndPost(long identificador,String post);

    List<Comment> findAllByPost(String post);

    Page<Comment> findAllByPost(String post,Pageable pageable);

    Page<Comment> findAllByAutor(String autor,Pageable pageable);

    Comment save(Comment entity);

    Comment findTopByOrderByIdentificadorDesc();

}