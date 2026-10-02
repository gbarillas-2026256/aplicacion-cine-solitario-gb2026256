package org.cinekinal.system.model;

/**
 * Movie model.
 */
public class Movie extends Pelicula {

    public Movie() {
        super();
    }

    public Movie(String idMovie, String title, String genre, String rating, int durationMin,
                 String synopsis, String posterUrl, String trailerUrl) {
        super(idMovie, title, genre, rating, durationMin, synopsis, posterUrl, trailerUrl);
    }

    public String getIdMovie() {
        return getIdPelicula();
    }

    public void setIdMovie(String idMovie) {
        setIdPelicula(idMovie);
    }

    public String getTitle() {
        return getTitulo();
    }

    public void setTitle(String title) {
        setTitulo(title);
    }

    public String getGenre() {
        return getGenero();
    }

    public void setGenre(String genre) {
        setGenero(genre);
    }

    public String getRating() {
        return getClasificacion();
    }

    public void setRating(String rating) {
        setClasificacion(rating);
    }

    public int getDurationMin() {
        return getDuracionMin();
    }

    public void setDurationMin(int durationMin) {
        super.setDuracionMin(durationMin);
    }

    public String getSynopsis() {
        return getSinopsis();
    }

    public void setSynopsis(String synopsis) {
        setSinopsis(synopsis);
    }

    public String getPosterUrl() {
        return super.getPosterUrl();
    }

    public void setPosterUrl(String posterUrl) {
        super.setPosterUrl(posterUrl);
    }

    public String getTrailerUrl() {
        return super.getTrailerUrl();
    }

    public void setTrailerUrl(String trailerUrl) {
        super.setTrailerUrl(trailerUrl);
    }
}
