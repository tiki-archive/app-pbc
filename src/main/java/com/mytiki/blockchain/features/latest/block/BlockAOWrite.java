/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.block;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockAOWrite {

    private String address;
    private String signature;
    private String data;

    @JsonCreator
    public BlockAOWrite(
            @JsonProperty(required = true) String address,
            @JsonProperty(required = true) String signature,
            @JsonProperty(required = true) String data) {
        this.address = address;
        this.signature = signature;
        this.data = data;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
