package com.samuel.etse.aos.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.fasterxml.jackson.annotation.JsonFormat;

@ApiModel(value = "Post", description = "Post in the blog")
@Document(collection = "posts")
public class Post {
    @ApiModelProperty(name = "id", value = "MongoDB Object ID", required = true)
    @Id
    private ObjectId id;
    @ApiModelProperty(name = "Title", value = "Title of a post", required = true)
    private String titulo;
    @ApiModelProperty(name = "Author", value = "Author of a post", required = true)
    private String autor;
    @ApiModelProperty(name = "Body", value = "Body of a post", required = true)
    private String cuerpo;
    @ApiModelProperty(name = "Summary", value = "Summary of a post", required = true)
    private String resumen;
    @ApiModelProperty(name = "Id", value = "Id of a post", required = true)
    private long identificador;
    @ApiModelProperty(name = "Key Words", value = "Key words of the post", required = true)
    private List<String> palabrasClave;
    @ApiModelProperty(name = "Date", value = "Date of the creation of the post", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fecha;

    public String getTitulo() {
        return titulo;
    }

    public Date getFecha() {
        return fecha;
    }

    public Post setFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }

    public List<String> getPalabrasClave() {
        return palabrasClave;
    }

    public Post setPalabrasClave(List<String> palabrasClave) {
        this.palabrasClave = palabrasClave;
        return this;
    }

    public long getIdentificador() {
        return identificador;
    }

    public Post setIdentificador(long identificador) {
        this.identificador = identificador;
        return this;
    }

    public String getResumen() {
        return resumen;
    }

    public Post setResumen(String resumen) {
        this.resumen = resumen;
        return this;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public Post setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
        return this;
    }

    public String getAutor() {
        return autor;
    }

    public Post setAutor(String autor) {
        this.autor = autor;
        return this;
    }

    public Post setTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

}