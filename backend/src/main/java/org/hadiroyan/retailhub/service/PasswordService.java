package org.hadiroyan.retailhub.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordService {

    public String hash(String rawPassword) {
        return BcryptUtil.bcryptHash(rawPassword);
    }

    public boolean verify(String rawPassword, String hashedPassword) {
        return BcryptUtil.matches(rawPassword, hashedPassword);
    }
}
