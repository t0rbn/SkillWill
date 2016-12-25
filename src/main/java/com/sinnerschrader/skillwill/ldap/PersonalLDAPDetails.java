package com.sinnerschrader.skillwill.ldap;

/**
 * Data Structure for Details from LDAP.
 *
 * @author torree
 */
public class PersonalLDAPDetails {

    private String firstName;
    private String lastName;
    private String mail;
    private String phone;
    private String location;
    private String title;

    public PersonalLDAPDetails(String firstName, String lastName, String mail, String telephone, String location, String title) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.phone = telephone;
        this.location = location;
        this.title = title;
    }

    public PersonalLDAPDetails() {
        this.firstName = null;
        this.lastName = null;
        this.mail = null;
        this.phone = null;
        this.location = null;
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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
