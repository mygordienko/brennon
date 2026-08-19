package com.mygordienko.brennon.core.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;

/**
 * Aggregate.
 * Represents a user profile:
 *    - personal data
 *    - business-related parameters
 *    - contacts
 *    - etc
 * 
 * This must not be confused with Spring's {@link org.springframework.security.core.userdetail.UserDetails}
 *    which represents a "technical" login (creds and authorities). The separation is intentional.
 *  
 * @param value String representation.
 */
@Table(name = "user_profiles")
public class BrennonUserProfile implements Persistable<BrennonUserId> {
    public BrennonUserProfile(BrennonUserId id, BrennonUserLogin login, String firstName, String lastName) {
        this.id = id;
        this.login = login;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Id
    @Getter
    @Column("id")
    private final BrennonUserId id;
    @Getter
    @Column("username")
    private final BrennonUserLogin login;
    @Getter
    @Column("first_name")
    private final String firstName;
    @Getter
    @Column("last_name")
    private final String lastName;

    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
