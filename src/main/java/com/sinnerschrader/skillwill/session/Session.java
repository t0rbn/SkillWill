package com.sinnerschrader.skillwill.session;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.data.annotation.Id;

/**
 * Sesion information
 *
 * @author torree
 */
public class Session {

    @Id
    private String key;
    private String username;
    private Date expireDate;

    public Session(String key, String username, Date expireDate) {
        this.key = key;
        this.username = username;
        this.expireDate = expireDate;
    }

    public String getKey() {
        return key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public Date getExpireDate() {
        return this.expireDate;
    }

    public void renewSession(int minutes) {
        // Spring Data does not fully support the java.time api.
        // The old Date is crap, so ZonedDatetime is used and then converted.
        // new expireDate = n Minutes from now on
        this.expireDate = Date.from(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(minutes).toInstant());
    }

    public boolean isExpired() {
        ZonedDateTime expireZonedDateTime = ZonedDateTime.ofInstant(this.expireDate.toInstant(), ZoneOffset.UTC);
        ZonedDateTime currentZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        return currentZonedDateTime.isAfter(expireZonedDateTime);
    }

}
