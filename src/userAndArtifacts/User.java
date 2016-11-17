package userAndArtifacts;

import java.io.Serializable;

/**
 * Created by tobiaj on 2016-11-10.
 */
public class User implements Serializable{
    //Serializable makes it possible to send object over network.
    private int age;
    private Name name;
    private Gender gender;
    private Occupation occupation;
    private Artifacts.Genre interest;
    private int yearInterest;

    private enum Gender {
        male,
        female
    }

    private enum Name {
        olle,
        pelle,
        kalle,
        lisa,
        kajsa

    }

    private enum Occupation {
        economic,
        engineer,
        journalist,
        athlete,
        criminal
    }

    public User(){
        int chooseGender = (int) (Math.random() * 1);
        int chooseOccupation = (int) (Math.random() * 4);
        int chooseGenre = (int) (Math.random() * 3);

        age = (int) (Math.random() * 100);
        yearInterest = (int) (Math.random() * 2016);
        gender = Gender.values()[chooseGender];
        occupation = Occupation.values()[chooseOccupation];
        interest = Artifacts.Genre.values()[chooseGenre];
        name = Name.values()[chooseOccupation];


    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Artifacts.Genre getInterest() {
        return interest;
    }

    public void setInterest(Artifacts.Genre interest) {
        this.interest = interest;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public int getYearInterest() {
        return yearInterest;
    }

    public void setYearInterest(int yearInterest) {
        this.yearInterest = yearInterest;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupasion) {
        this.occupation = occupation;
    }

}
