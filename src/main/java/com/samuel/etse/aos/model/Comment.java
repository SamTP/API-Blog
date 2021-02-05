package com.samuel.etse.aos.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Comment", description = "Comment in a post")
@Document(collection = "comments")
public class Comment {

    @ApiModelProperty(name = "id", value = "MongoDB Object ID", required = true)
    @Id
    private ObjectId id;
    @ApiModelProperty(name = "Post", value = "Title of the post the comment is on", required = true)
    private String post;
    @ApiModelProperty(name = "Author", value = "Author of the comment", required = true)
    private String autor;
    @ApiModelProperty(name = "Id", value = "Id of the comment per post", required = true)
    private long identificador;
    @ApiModelProperty(name = "Body", value = "Body of the comment", required = true)
    private String cuerpo;
    @ApiModelProperty(name = "Date", value = "Date of the post", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fecha;

    public Date getFecha() {
        return fecha;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public Comment setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
        return this;
    }

    public Comment setFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }

    public String getPost() {
        return post;
    }

    public long getIdentificador() {
        return identificador;
    }

    public Comment setIdentificador(long identificador) {
        this.identificador = identificador;
        return this;
    }

    public String getAutor() {
        return autor;
    }

    public Comment setAutor(String autor) {
        this.autor = autor;
        return this;
    }

    public Comment setPost(String post) {
        this.post = post;
        return this;
    }

}