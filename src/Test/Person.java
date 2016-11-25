/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author bewa1022
 */
public class Person {
// An enum for age categories

    public enum AgeCategory {

        BABY, CHILD, TEEN, ADULT, SENIOR, UNKNOWN
    };
    private final ReadOnlyIntegerWrapper personId
            = new ReadOnlyIntegerWrapper(this, "personId", personSequence.incrementAndGet());
    private final StringProperty firstName
            = new SimpleStringProperty(this, "firstName", null);
    private final StringProperty lastName
            = new SimpleStringProperty(this, "lastName", null);
    private final ObjectProperty<LocalDate> birthDate
            = new SimpleObjectProperty<>(this, "birthDate", null);
// Keeps track of last generated person id
    private static AtomicInteger personSequence = new AtomicInteger(0);

    public Person() {
        this(null, null, null);
    }

    public Person(String firstName, String lastName, LocalDate birthDate) {
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.birthDate.set(birthDate);
    }
    /* personId Property */

    public final int getPersonId() {
        return personId.get();
    }

    public final ReadOnlyIntegerProperty personIdProperty() {
        return personId.getReadOnlyProperty();
    }
    /* firstName Property */

    public final String getFirstName() {
        return firstName.get();
    }

    public final void setFirstName(String firstName) {
        firstNameProperty().set(firstName);

    }

    public final StringProperty firstNameProperty() {
        return firstName;
    }
    /* lastName Property */

    public final String getLastName() {
        return lastName.get();
    }

    public final void setLastName(String lastName) {
        lastNameProperty().set(lastName);
    }

    public final StringProperty lastNameProperty() {
        return lastName;
    }
    /* birthDate Property */

    public final LocalDate getBirthDate() {
        return birthDate.get();
    }

    public final void setBirthDate(LocalDate birthDate) {
        birthDateProperty().set(birthDate);
    }

    public final ObjectProperty<LocalDate> birthDateProperty() {
        return birthDate;
    }
    /* Domain specific business rules */

    public AgeCategory getAgeCategory() {
        if (birthDate.get() == null) {
            return AgeCategory.UNKNOWN;
        }
        long years = java.time.temporal.ChronoUnit.YEARS.between(birthDate.get(), LocalDate.now());
        if (years >= 0 && years < 2) {
            return AgeCategory.BABY;
        } else if (years >= 2 && years < 13) {
            return AgeCategory.CHILD;
        } else if (years >= 13 && years <= 19) {
            return AgeCategory.TEEN;
        } else if (years > 19 && years <= 50) {
            return AgeCategory.ADULT;
        } else if (years > 50) {
            return AgeCategory.SENIOR;
        } else {
            return AgeCategory.UNKNOWN;
        }
    }
}
