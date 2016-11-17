package userAndArtifacts;

import java.io.Serializable;

/**
 * Created by tobiaj on 2016-11-10.
 */
public class Artifacts implements Serializable {
    private int id;
    private Name name;
    private Creator creator;
    private int dateOfCreation;
    private Country placeOfCreation;
    private Genre genre;

    public enum Genre {
        Painting,
        Pictures,
        Sculpture,
        Photos
    }

    public enum Creator {
        picasso,
        daVinci,
        michelangelo,
        donatello,
        rafael,
        donald,
        minnie
    }

    public enum Name {
        monalisa,
        monkey,
        lion,
        tiger,
        elephant,
        dog,
        cat
    }

    public enum Country {
        italy,
        sweden,
        spain,
        greece,
        france,
        england,
        germany
    }

    public Artifacts(){
        int rand = (int) (Math.random() * 100);
        int chooseGenre = (int) (Math.random() * 3);
        int choose = (int) (Math.random() * 6);

        id = rand;
        name = Name.values()[choose];
        creator = Creator.values()[choose];
        dateOfCreation = (int) (Math.random() * 2016);
        placeOfCreation = Country.values()[choose];
        genre = Genre.values()[chooseGenre];
    }


    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public int getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(int dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Country getPlaceOfCreation() {
        return placeOfCreation;
    }

    public void setPlaceOfCreation(Country placeOfCreation) {
        this.placeOfCreation = placeOfCreation;
    }

}
