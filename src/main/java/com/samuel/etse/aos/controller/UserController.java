package com.samuel.etse.aos.controller;

import com.samuel.etse.aos.model.Comment;
import com.samuel.etse.aos.model.auth.BaseUser;
import com.samuel.etse.aos.model.User;

import com.samuel.etse.aos.repository.CommentRepository;
import com.samuel.etse.aos.repository.PostRepository;
import com.samuel.etse.aos.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.xml.ws.Response;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Api(tags = "User controller", description = "REST API for user operations", produces = "application/json", consumes = "application/json")
@RestController
@RequestMapping("users")
public class UserController {

    private UserRepository db;
    private PostRepository dbP;
    private CommentRepository dbC;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository db, CommentRepository dbC, PostRepository dbP,
                          PasswordEncoder passwordEncoder) {
        this.db = db;
        this.dbP = dbP;
        this.dbC = dbC;
        this.passwordEncoder = passwordEncoder;

    }

    @ApiOperation("Get list of users  or users with role redactor")
    @ApiResponses(@ApiResponse(code = 200, message = "List of users", response = User.class))
    @PreAuthorize("permitAll()")
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Page<User>> getAllUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                                  @RequestParam(value = "size", defaultValue = "6") int size,
                                                  @RequestParam(value = "rol", required = false) String rol) {
        Page<User> users;
        if (rol != null)
            users = db.findAllByRolesContains(rol, PageRequest.of(page, size)).map(user -> user.setPassword(null));
        else
            users = db.findAll(PageRequest.of(page, size)).map(user -> user.setPassword(null));
        return ResponseEntity.ok().body(users);
    }

    @ApiOperation("Get a specific user")
    @ApiResponses({@ApiResponse(code = 200, message = "Details of an user", response = User.class),
            @ApiResponse(code = 404, message = "User not found")})
    @PreAuthorize("permitAll()")
    @GetMapping(path = "/{username}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BaseUser> getUser(@PathVariable("username") String username) {
        if (db.existsByUsername(username))
            return ResponseEntity.ok().body(db.findByUsername(username).setPassword("*******"));
        else
            return ResponseEntity.notFound().build();
    }

    @ApiOperation("Get all comments from user")
    @ApiResponses({
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 200, message = "List of user comments")
    })
    @GetMapping(path = "{username}/comments")
    public ResponseEntity<Page<Comment>> getUserComments(@RequestParam(value = "page", defaultValue = "0") int page,
                                                         @RequestParam(value = "size", defaultValue = "3") int size, @PathVariable("username") String username) {
        Page<Comment> comments;
        if (db.existsByUsername(username))
            comments = dbC.findAllByAutor(username, PageRequest.of(page, size));
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        return ResponseEntity.status(HttpStatus.OK).body(comments);

    }

    @ApiOperation("Delete a specific user")
    @ApiResponses({@ApiResponse(code = 204, message = "User deleted"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 401, message = "This action needs and active session"),
            @ApiResponse(code = 403, message = "You do not have permission to perfom this action")})
    @PreAuthorize("hasRole('LECTOR')")
    @DeleteMapping(path = "/{username}")
    public ResponseEntity<User> delete(@PathVariable("username") String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        User user = db.findByUsername(name);
        if (!db.existsByUsername(username))
            return ResponseEntity.notFound().build();
        else {
            if (user.getRoles().contains("ADMIN")) {
                dbC.deleteByAutor(username);
                dbP.deleteByAutor(username);
                db.deleteByUsername(username);
            } else {
                if (username.equals(name)) {
                    dbC.deleteByAutor(username);
                    dbP.deleteByAutor(username);
                    db.deleteByUsername(username);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @ApiOperation("Creates a user in the app")
    @ApiResponses({@ApiResponse(code = 201, message = "User created"),
            @ApiResponse(code = 409, message = "User creation conflict")})
    @PreAuthorize("permitAll()")
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<URI> create(@RequestBody User datos) {
        if (db.existsByUsername(datos.getUsername()))
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        else {
            User user = new User();
            user.setUsername(datos.getUsername());
            user.setPassword(passwordEncoder.encode(datos.getPassword()));
            user.setRoles(datos.getRoles());
            user.setSuscripciones(datos.getSuscripciones());
            user.setActive(datos.isActive());
            user.setEmail(datos.getEmail());
            db.save(user);

            URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/{username}")
                    .buildAndExpand(user.getUsername()).toUri();
            return ResponseEntity.created(location).body(location);
        }
    }

    // --> @JsonAnySetter

    @ApiOperation("Updates information about the user")
    @ApiResponses({@ApiResponse(code = 200, message = "Information updated"),
            @ApiResponse(code = 404, message = "User not found"), @ApiResponse(code = 400, message = "Bad request")})
    @PreAuthorize("hasRole('LECTOR')")
    @PutMapping(path = "/{username}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<User> updateActive(@RequestBody User datos, @RequestParam(value = "mod") String value, @RequestParam(value = "sub", required = false) String sub, @RequestParam(value = "tipo", required = false) String tipo) {
        if (!db.existsByUsername(datos.getUsername()))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        else {
            User user = db.findByUsername(datos.getUsername());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String name = auth.getName();
            User superUser = db.findByUsername(name);
            switch (value) {
                case "active":
                    if (superUser.getRoles().contains("MODERADOR"))
                        user.setActive(datos.isActive());
                    else
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    break;
                case "roles":
                    if (superUser.getRoles().contains("ADMIN"))
                        user.setRoles(datos.getRoles());
                    else
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    break;

                case "subs":
                    List<String> temas = user.getSuscripciones().get(0);
                    List<String> usuarios = user.getSuscripciones().get(1);
                    if (tipo.equals("temas")) {
                        if (temas.contains(sub))
                            temas.remove(temas.indexOf(sub));
                        else
                            temas.add(sub);
                    } else {
                        if (usuarios.contains(sub))
                            usuarios.remove(usuarios.indexOf(sub));
                        else
                            usuarios.add(sub);
                    }
                    List<List<String>> update = user.getSuscripciones();
                    update.set(0, temas);
                    update.set(1, usuarios);
                    datos.setSuscripciones(update);
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            db.save(user);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

}