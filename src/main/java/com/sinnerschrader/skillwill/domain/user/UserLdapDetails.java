package com.sinnerschrader.skillwill.domain.user;

import com.unboundid.ldap.sdk.SearchResultEntry;

/**
 * Data Structure for Details from LDAP.
 *
 * @author torree
 */
public class UserLdapDetails {

  private final String firstName;
  private final String lastName;
  private final String mail;
  private final String phone;
  private final String location;
  private final String title;

  public UserLdapDetails(String firstName, String lastName, String mail, String telephone,
      String location, String title) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.mail = mail;
    this.phone = telephone;
    this.location = location;
    this.title = title;
  }

  public UserLdapDetails() {
    this(null, null, null, null, null, null);
  }

  public UserLdapDetails(SearchResultEntry entry) {
    this(
      entry.getAttributeValue("givenName"),
      entry.getAttributeValue("sn"),
      entry.getAttributeValue("mail"),
      entry.getAttributeValue("telephoneNumber"),
      entry.getAttributeValue("l"),
      entry.getAttributeValue("title")
    );
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getMail() {
    return mail;
  }

  public String getPhone() {
    return phone;
  }

  public String getLocation() {
    return location;
  }

  public String getTitle() {
    return title;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    UserLdapDetails that = (UserLdapDetails) o;

    if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) {
      return false;
    }
    if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) {
      return false;
    }
    if (mail != null ? !mail.equals(that.mail) : that.mail != null) {
      return false;
    }
    if (phone != null ? !phone.equals(that.phone) : that.phone != null) {
      return false;
    }
    if (location != null ? !location.equals(that.location) : that.location != null) {
      return false;
    }
    return title != null ? title.equals(that.title) : that.title == null;
  }

}
