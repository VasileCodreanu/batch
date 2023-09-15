package com.cedacri.batchstart.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class PersonRequestDto {

    @NotEmpty(message = "lastName can not be empty")
    private String lastName;
    @NotEmpty(message = "firstName can not be empty")
    @Size(min = 2, max = 20, message = "firstName Me must be between 2 and 20 characters")
    private String firstName;

    public PersonRequestDto() {
    }

    public PersonRequestDto(String lastName, String firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String toString() {
        return "{lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                '}';
    }

}
