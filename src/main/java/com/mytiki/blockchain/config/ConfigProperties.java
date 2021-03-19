/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.config;

import org.springframework.beans.factory.annotation.Value;

public class ConfigProperties {

    @Value("${spring.profiles.active:}")
    private String springProfilesActive;

    @Value("${com.mytiki.blockchain.jwt.public_key}")
    private String jwtPublicKey;

    public String getSpringProfilesActive() {
        return springProfilesActive;
    }

    public void setSpringProfilesActive(String springProfilesActive) {
        this.springProfilesActive = springProfilesActive;
    }

    public String getJwtPublicKey() {
        return jwtPublicKey;
    }

    public void setJwtPublicKey(String jwtPublicKey) {
        this.jwtPublicKey = jwtPublicKey;
    }
}
