package com.cedacri.batchstart.batch.job.InvalidDataStep;

public class InvalidPersonRequestDto {
    private String firstName;
    private String lastName;

    private String occurredError;

    public InvalidPersonRequestDto() {
    }

    public InvalidPersonRequestDto(String firstName, String lastName, String occurredError) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.occurredError = occurredError;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOccurredError() {
        return occurredError;
    }

    public void setOccurredError(String occurredError) {
        this.occurredError = occurredError;
    }

    @Override
    public String toString() {
        return "InvalidPersonRequestDto{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", occurredError='" + occurredError + '\'' +
                '}';
    }
}
