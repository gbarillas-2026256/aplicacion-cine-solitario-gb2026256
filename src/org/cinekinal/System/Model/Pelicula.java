package org.cinekinal.system.model;

public class Pelicula {
    private String idPelicula;
    private String titulo;
    private String genero;
    private String clasificacion;
    private int duracionMin;
    private String sinopsis;
    private String posterUrl;
    private String trailerUrl;

    public Pelicula() {
    }

    public String getIdPelicula() {
        return idPelicula;
    }

    public void setIdPelicula(String idPelicula) {
        this.idPelicula = idPelicula;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    public void setDuracionMin(int duracionMin) {
        this.duracionMin = duracionMin;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    // English alias methods
    public String getMovieId() { return idPelicula; }
    public String getTitle() { return titulo; }
    public String getGenre() { return genero; }
    public String getClassification() { return clasificacion; }
    public int getDurationMinutes() { return duracionMin; }
    public String getSynopsis() { return sinopsis; }
}
