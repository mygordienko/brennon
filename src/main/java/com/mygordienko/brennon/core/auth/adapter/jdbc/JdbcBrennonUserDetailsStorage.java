package com.mygordienko.brennon.core.auth.adapter.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class JdbcBrennonUserDetailsStorage implements UserDetailsService {
    
    public JdbcBrennonUserDetailsStorage(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final var parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("target", username);

        var sql = """
            select u.username, u.enabled, a.authority, u.password
            from user_logins u
                left join user_authorities a on u.username = a.username
            where u.username = :target
                """;

        UserDetails user = jdbcTemplate.query(
            sql, 
            parameterSource, 
            new SingleUserExtractor()
        );

        return Optional.ofNullable(user).orElseThrow(() -> UsernameNotFoundException.fromUsername(username));
    }

    private static class SingleUserExtractor implements ResultSetExtractor<UserDetails> {
        @Override
        public UserDetails extractData(ResultSet rs) throws SQLException, DataAccessException {
            UserDetails user = null;
            var authorities = new HashSet<GrantedAuthority>();

            while (rs.next()) {
                var login = rs.getString(1);
                var active = rs.getBoolean(2);
                var password = rs.getString(4);
                // Initialize the user object once on the first row
                if (user == null) {
                    user = User.builder()
                    .username(login)
                    .password(password)
                    .accountExpired(!active)
                    .authorities(new HashSet<>())
                    .build();
                }

                // Append authorities from each row (handles null if user has no roles)
                String authority = rs.getString(3);
                if (authority != null) {
                    authorities.add(new SimpleGrantedAuthority(authority));
                }
            }

            if (user == null) {
                return null;
            }

            return User.withUserDetails(user)
            .authorities(authorities)
            .build();
        }
    }
}
