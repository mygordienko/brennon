package com.mygordienko.brennon.profile;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.common.FileId;

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
public class BrennonUserProfile extends AbstractAggregateRoot<BrennonUserProfile> implements Persistable<BrennonUserId> {
    public BrennonUserProfile(
            BrennonUserId id,
            Integer version,
            BrennonUserLogin login,
            String firstName,
            String lastName,
            FileId profileImageFileId,
            OffsetDateTime created,
            OffsetDateTime updated) {
        this.id = id;
        this.version = version;
        this.login = login;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileImageFileId = profileImageFileId;
        this.created = created;
        this.updated = updated;
    }

    @Id
    @Getter
    @Column("id")
    private final BrennonUserId id;
    @Getter
    @Version
    @Column("version")
    private final Integer version;
    @Getter
    @Column("username")
    private final BrennonUserLogin login;
    @Getter
    @Column("first_name")
    private final String firstName;
    @Getter
    @Column("last_name")
    private final String lastName;
    @Column("profile_image_file_id")
    private FileId profileImageFileId;
    @Getter
    @Column("created")
    private final OffsetDateTime created;
    @Getter
    @Column("updated")
    private OffsetDateTime updated;

    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    public Optional<FileId> getProfileImageFileId() {
        return Optional.ofNullable(profileImageFileId);
    }

    public void updateProfileImage(BrennonUserId userId, FileId fileId, OffsetDateTime updated) {
        if (!this.id.equals(userId)) {
            throw new IllegalStateException("User %s can not update profile %s".formatted(userId, this.id));
        }

        if (fileId == null) {
            throw new IllegalArgumentException("Profile image file ID is required");
        }

        var previousFileId = this.profileImageFileId;
        this.profileImageFileId = fileId;
        this.updated = updated;

        registerEvent(new ProfileImageUpdatedEvent(this.id, previousFileId, fileId));
    }

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
