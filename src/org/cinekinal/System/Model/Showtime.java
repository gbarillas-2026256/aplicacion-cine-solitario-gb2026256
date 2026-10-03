package org.cinekinal.system.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

/**
 * Showtime entity representing a movie screening schedule.
 */
public class Showtime {
    private String idShowtime;
    private String theaterId;
    private String movieTitle;
    private int durationMin;
    private String theaterName;
    private String theaterType;
    private Date showDate;
    private Time showTime;
    private BigDecimal basePrice;
    private String genre;
    private String rating;
    private String synopsis;
    private String posterUrl;
    private String trailerUrl;
    private String movieId;

    public Showtime() {
    }

    public String getIdShowtime() {
        return idShowtime;
    }

    public void setIdShowtime(String idShowtime) {
        this.idShowtime = idShowtime;
    }

    public String getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(String theaterId) {
        this.theaterId = theaterId;
    }

    public String getIdTheater() { return theaterId; }
    public void setIdTheater(String id) { this.theaterId = id; }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public int getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getTheaterType() {
        return theaterType;
    }

    public void setTheaterType(String theaterType) {
        this.theaterType = theaterType;
    }

    public Date getShowDate() {
        return showDate;
    }

    public void setShowDate(Date showDate) {
        this.showDate = showDate;
    }

    public Date getDate() { return showDate; }
    public void setDate(Date date) { this.showDate = date; }

    public Time getShowTime() {
        return showTime;
    }

    public void setShowTime(Time showTime) {
        this.showTime = showTime;
    }

    public Time getTime() { return showTime; }
    public void setTime(Time time) { this.showTime = time; }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
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

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

}
