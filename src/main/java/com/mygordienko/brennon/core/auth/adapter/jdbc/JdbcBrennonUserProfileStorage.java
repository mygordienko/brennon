package com.mygordienko.brennon.core.auth.adapter.jdbc;

import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.core.auth.BrennonUserProfileStoragePort;

public class JdbcBrennonUserProfileStorage implements BrennonUserProfileStoragePort {
    public JdbcBrennonUserProfileStorage(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final NamedParameterJdbcTemplate jdbcTemplate;


    @Override
    public Optional<BrennonUserProfile> getById(BrennonUserId userId) {
        final var parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("id", userId.toString());

        var sql = """
            select u.first_name, u.last_name, u.id, u.username
            from user_profiles u
            where u.id = :id
                """;

        BrennonUserProfile user = jdbcTemplate.query(
            sql, 
            parameterSource,
            (rs) -> {

                if (!rs.next()) {
                    return null;
                }

                var firstName = rs.getString(1);
                var lastName = rs.getString(2);
                var id = BrennonUserId.of(rs.getString(3));
                var login = BrennonUserLogin.of(rs.getString(4));

                return new BrennonUserProfile(id, login, firstName, lastName);
            }
        );

        return Optional.ofNullable(user);
    }

    @Override
    public Optional<BrennonUserProfile> getByLogin(BrennonUserLogin username) {
        final var parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("username", username.toString());

        var sql = """
            select u.first_name, u.last_name, u.id, u.username
            from user_profiles u
            where u.username = :username
                """;

        BrennonUserProfile user = jdbcTemplate.query(
            sql, 
            parameterSource,
            (rs) -> {

                if (!rs.next()) {
                    return null;
                }

                var firstName = rs.getString(1);
                var lastName = rs.getString(2);
                var id = BrennonUserId.of(rs.getString(3));
                var login = BrennonUserLogin.of(rs.getString(4));

                return new BrennonUserProfile(id, login, firstName, lastName);
            }
        );

        return Optional.ofNullable(user);
    }
}
