package org.cinekinal.system.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

/**
 * Showtime projection model with resolved movie and theater details.
 */
public class Showtime extends Funcion {

    public Showtime() {
        super();
    }

    public String getIdShowtime() {
        return getIdFuncion();
    }

    public void setIdShowtime(String idShowtime) {
        setIdFuncion(idShowtime);
    }

    public String getIdTheater() {
        return getIdSala();
    }

    public void setIdTheater(String idTheater) {
        setIdSala(idTheater);
    }

    public String getMovieTitle() {
        return getTituloPelicula();
    }

    public void setMovieTitle(String movieTitle) {
        setTituloPelicula(movieTitle);
    }

    public int getDurationMin() {
        return getDuracionMin();
    }

    public void setDurationMin(int durationMin) {
        setDuracionMin(durationMin);
    }

    public String getTheaterName() {
        return getNombreSala();
    }

    public void setTheaterName(String theaterName) {
        setNombreSala(theaterName);
    }

    public String getTheaterType() {
        return getTipoSala();
    }

    public void setTheaterType(String theaterType) {
        setTipoSala(theaterType);
    }

    public Date getDate() {
        return getFecha();
    }

    public void setDate(Date date) {
        setFecha(date);
    }

    public Time getTime() {
        return getHora();
    }

    public void setTime(Time time) {
        setHora(time);
    }

    public BigDecimal getBasePrice() {
        return getPrecioBase();
    }

    public void setBasePrice(BigDecimal basePrice) {
        setPrecioBase(basePrice);
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
