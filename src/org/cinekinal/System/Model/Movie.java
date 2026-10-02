package org.cinekinal.system.model;

/**
 * Movie entity representing a film in the catalog.
 */
public class Movie {
    private String idMovie;
    private String title;
    private String genre;
    private String rating;
    private int durationMin;
    private String synopsis;
    private String posterUrl;
    private String trailerUrl;

    public Movie() {
    }

    public Movie(String idMovie, String title, String genre, String rating, int durationMin,
                 String synopsis, String posterUrl, String trailerUrl) {
        this.idMovie = idMovie;
        this.title = title;
        this.genre = genre;
        this.rating = rating;
        this.durationMin = durationMin;
        this.synopsis = synopsis;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
    }

    public String getIdMovie() {
        return idMovie;
    }

    public void setIdMovie(String idMovie) {
        this.idMovie = idMovie;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
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

    // Compatibility getters for TableView properties if needed
    public String getIdPelicula() { return idMovie; }
    public String getTitulo() { return title; }
    public String getGenero() { return genre; }
    public String getClasificacion() { return rating; }
    public String getSinopsis() { return synopsis; }
}
