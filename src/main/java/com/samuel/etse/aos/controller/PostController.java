package com.samuel.etse.aos.controller;

import com.samuel.etse.aos.model.Post;
import com.samuel.etse.aos.repository.PostRepository;
import com.samuel.etse.aos.repository.CommentRepository;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Api(tags = "Post controller", description = "REST API for post operations", produces = "application/json", consumes = "application/json")
@RestController
@RequestMapping("posts")
public class PostController {

    private PostRepository db;
    private CommentRepository dbC;

    @Autowired
    public PostController(PostRepository db, CommentRepository dbC) {
        this.db = db;
        this.dbC = dbC;
    }

    // --> Todos los post de un autor o todos los post por autor
    @ApiOperation("Returns all post from DB or all post form specific author")
    @ApiResponses(
            @ApiResponse(code = 200, message = "List of posts", response = Post.class)
    )
    @PreAuthorize("permitAll()")
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Page<Post>> getAllPostByAutor(@RequestParam(value = "page", defaultValue = "0") int page,
                                                        @RequestParam(value = "size", defaultValue = "6") int size,
                                                        @RequestParam(value = "autor", required = false) String autor) {
        Page<Post> posts;
        if (autor == null)
            posts = db.findAll(PageRequest.of(page, size));
        else
            posts = db.findAllByAutor(autor, PageRequest.of(page, size));
        return ResponseEntity.ok().body(posts);

    }

    @ApiOperation("Returns a specific post")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns a specific post", response = Post.class),
            @ApiResponse(code = 404, message = "Post not found")
    })
    @PreAuthorize("permitAll()")
    @GetMapping(path = "/{idPost}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Post> getPost(@PathVariable("idPost") long identificador) {
        if (db.existsByIdentificador(identificador))
            return ResponseEntity.ok().body(db.findByIdentificador(identificador));
        else
            return ResponseEntity.notFound().build();
    }

    @ApiOperation("Deletes a post form DB")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Post deleted"),
            @ApiResponse(code = 404, message = "Post not found"),
            @ApiResponse(code = 403, message = "You do not have permission")
    })
    @PreAuthorize("hasRole('REDACTOR')")
    @DeleteMapping(path = "/{idPost}")
    public ResponseEntity<Post> delete(@PathVariable("idPost") long identificador) {
        if (!db.existsByIdentificador(identificador))
            return ResponseEntity.notFound().build();
        else {
            dbC.deleteByPost(String.valueOf(identificador));
            db.deleteByIdentificador(identificador);
            return ResponseEntity.noContent().build();
        }
    }

    @ApiOperation("Creates a post")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Post created"),
            @ApiResponse(code = 409, message = "Conflict in the creation of the post"),
            @ApiResponse(code = 403, message = "You do not have permission to make a post")
    })
    @PreAuthorize("hasRole('REDACTOR')")
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<URI> create(@RequestBody Post datos) {
        if (db.existsByIdentificador(datos.getIdentificador()))
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        else {
            Post post = new Post();
            Post aux = db.findTopByOrderByIdentificadorDesc();
            if (aux != null)
                post.setIdentificador(aux.getIdentificador() + 1);
            else
                post.setIdentificador(1);
            post.setAutor(datos.getAutor());
            post.setCuerpo(datos.getCuerpo());
            post.setResumen(datos.getResumen());
            post.setTitulo(datos.getTitulo());
            System.out.println(datos);
            post.setPalabrasClave(datos.getPalabrasClave());
            post.setFecha(datos.getFecha());

            db.save(post);

            URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/posts/{idPost}")
                    .buildAndExpand(post.getIdentificador()).toUri();
            return ResponseEntity.created(location).body(location);
        }
    }

    @ApiOperation("Update a post")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Post updated"),
            @ApiResponse(code = 404, message = "Post not found"),
            @ApiResponse(code = 403, message = "You do not have permission to update this post")
    })
    @PreAuthorize("hasRole('REDACTOR')")
    @PutMapping(path = "/{idPost}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Post> update(@PathVariable long idPost, @RequestBody Post datos
    ) {
        if (!db.existsByIdentificador(idPost))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        else {
            Post post = db.findByIdentificador(idPost);
            post.setTitulo(datos.getTitulo());
            post.setResumen(datos.getResumen());
            post.setCuerpo(datos.getCuerpo());
            db.save(post);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

}