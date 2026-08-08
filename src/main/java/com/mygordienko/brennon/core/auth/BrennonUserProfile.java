package com.mygordienko.brennon.core.auth;

/**
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
public record BrennonUserProfile(
   BrennonUserId id,
   BrennonUserLogin login,
   String firstName,
   String lastName
) {
}
