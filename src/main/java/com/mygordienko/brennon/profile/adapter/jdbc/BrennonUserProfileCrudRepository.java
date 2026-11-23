package com.mygordienko.brennon.profile.adapter.jdbc;

import org.springframework.data.repository.CrudRepository;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.profile.BrennonUserProfile;

public interface BrennonUserProfileCrudRepository extends CrudRepository<BrennonUserProfile, BrennonUserId> {
}
