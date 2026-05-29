package com.seiyrikon.seiyrikon_security.service;

import com.seiyrikon.seiyrikon_security.domain.SeiyrikonSecurityUser;

public interface SeiyrikonSecurityAuthProvider {

    /**
     *
     * Find user by username or email
     * Return null if not found
     *
     * @param identifier the username or email
     * @return SeiyrikonSecurityUser or null
     */
    SeiyrikonSecurityUser findUser(String identifier);

    /**
     *
     * Verify the raw password against the stored hashed password.
     *
     * @param rawPassword password given by user in payload
     * @param hashedPassword hashed password fetched from the database
     * @return boolean
     */
    boolean verifyPassword(String rawPassword, String hashedPassword);
}
