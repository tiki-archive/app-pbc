/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.address;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddressAOIssue {

    private String dataKey;
    private String signKey;
    private String referFrom;

    @JsonCreator
    public AddressAOIssue(
            @JsonProperty(required = true) String dataKey,
            @JsonProperty(required = true) String signKey,
            @JsonProperty String referFrom
    ) {
        this.dataKey = dataKey;
        this.signKey = signKey;
        this.referFrom = referFrom;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getSignKey() {
        return signKey;
    }

    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }

    public String getReferFrom() {
        return referFrom;
    }

    public void setReferFrom(String referFrom) {
        this.referFrom = referFrom;
    }
}
