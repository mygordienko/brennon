package com.mygordienko.brennon.core.auth.adapter.jdbc;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.core.auth.BrennonUserProfileStoragePort;

public class JdbcBrennonUserProfileStorage implements BrennonUserProfileStoragePort {
    public JdbcBrennonUserProfileStorage(JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    private final JdbcAggregateTemplate jdbcAggregateTemplate;


    @Override
    public Optional<BrennonUserProfile> getById(BrennonUserId userId) {
        var query = Query.query(Criteria.where("id").is(userId));
        return jdbcAggregateTemplate.findOne(query, BrennonUserProfile.class);
    }

    @Override
    public Optional<BrennonUserProfile> getByLogin(BrennonUserLogin username) {
        var query = Query.query(Criteria.where("username").is(username));
        return jdbcAggregateTemplate.findOne(query, BrennonUserProfile.class);
    }

    @Override
    public List<BrennonUserProfile> getByIds(Collection<BrennonUserId> userIds) {
        var query = Query.query(Criteria.where("id").in(userIds));
        return jdbcAggregateTemplate.findAll(query, BrennonUserProfile.class).stream()
            .sorted(Comparator.comparing(BrennonUserProfile::getId))
            .toList();
    }
}
