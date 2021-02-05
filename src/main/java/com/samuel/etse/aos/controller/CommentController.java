package com.samuel.etse.aos.controller;

import java.net.URI;
import java.util.List;

import com.samuel.etse.aos.model.Comment;

import com.samuel.etse.aos.model.User;
import com.samuel.etse.aos.repository.CommentRepository;

import com.samuel.etse.aos.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Api(tags = "Comment controller", description = "REST API for comment operations", produces = "application/json", consumes = "application/json")
@RestController
@RequestMapping("posts/{idPost}/comments")
public class CommentController {

    private CommentRepository db;
    private UserRepository dbU;

    @Autowired
    public CommentController(CommentRepository db, UserRepository dbU) {
        this.db = db;
        this.dbU = dbU;
    }


    @ApiOperation("Gets all comments from a post")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of comments")
    })
    @PreAuthorize("permitAll()")
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Page<Comment>> getAllComment(@PathVariable("idPost") String idPost,
                                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Comment> comments = db.findAllByPost(idPost, PageRequest.of(page, size));
        return ResponseEntity.ok().body(comments);
    }

    @ApiOperation("Gets a specific comment")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns a comment"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PreAuthorize("permitAll()")
    @GetMapping(path = "/{idComment}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Comment> getUser(@PathVariable("idComment") long identificador,
                                           @PathVariable("idPost") String idPost) {
        if (db.existsByIdentificadorAndPost(identificador, idPost))
            return ResponseEntity.ok().body(db.findByIdentificadorAndPost(identificador, idPost));
        else
            return ResponseEntity.notFound().build();
    }

    @ApiOperation("Deletes a specific comment")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Comment deleted"),
            @ApiResponse(code = 404, message = "Comment not found"),
            @ApiResponse(code = 401, message = "You must be logged in"),
            @ApiResponse(code = 403, message = "You do not have permission to delete this comment")
    })
    @PreAuthorize("hasRole('LECTOR')")
    @DeleteMapping(path = "/{idComment}")
    public ResponseEntity<Comment> delete(@PathVariable("idComment") long identificador,
                                          @PathVariable("idPost") String idPost) {
        if (!db.existsByIdentificadorAndPost(identificador, idPost))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String name = auth.getName();
            User user = dbU.findByUsername(name);
            Comment comment = db.findByIdentificadorAndPost(identificador, idPost);

            if (user.getUsername().equals(comment.getAutor())) {
                db.delete(comment);
            } else {
                if (user.getRoles().contains("MODERADOR"))
                    db.delete(comment);
                else
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @ApiOperation("Creates a comment in a post")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Returns URI of new post"),
            @ApiResponse(code = 401, message = "An active session is needed"),
            @ApiResponse(code = 403, message = "You do not have permission")
    })
    @PreAuthorize("hasRole('LECTOR')")
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<URI> create(@RequestBody Comment datos, @PathVariable("idPost") String idPost) {

        Comment comment = new Comment();
        List<Comment> aux = db.findAllByPost(idPost);
        if (aux.size() > 0)
            comment.setIdentificador((aux.get(aux.size() - 1).getIdentificador()) + 1);
        else
            comment.setIdentificador(1);

        comment.setAutor(datos.getAutor());
        comment.setFecha(datos.getFecha());
        comment.setPost(String.valueOf(idPost));
        comment.setCuerpo(datos.getCuerpo());
        db.save(comment);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("posts/{idPost}/comments/{idComment}")
                .buildAndExpand(idPost, comment.getIdentificador()).toUri();
        return ResponseEntity.created(location).body(location);

    }

    @ApiOperation("Updates a specific comment")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Comment updated"),
            @ApiResponse(code = 404, message = "Comment not found"),
            @ApiResponse(code = 401, message = "An active session is needed"),
            @ApiResponse(code = 403, message = "You do not have permission")
    })
    @PutMapping(path = "/{idComment}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Comment> update(@RequestBody Comment datos, @PathVariable("idPost") String idPost,@PathVariable("idComment") long identificador) {
        if (!db.existsByIdentificadorAndPost(datos.getIdentificador(), idPost))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String name = auth.getName();
            User user = dbU.findByUsername(name);
            Comment comment = db.findByIdentificadorAndPost(identificador, idPost);
            if (user.getUsername().equals(comment.getAutor())) {
                comment.setCuerpo(datos.getCuerpo());
            } else {
                if (user.getRoles().contains("MODERADOR"))
                    comment.setCuerpo(datos.getCuerpo());
                else
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            db.save(comment);

            return ResponseEntity.ok().body(comment);
        }
    }

}