/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.address;

import com.mytiki.common.exception.ApiExceptionFactory;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AddressRepository addressRepository;

    private static final String FAILED_ISSUE_MSG = "Failed to issue address";

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public AddressAORsp issue(AddressAOIssue addressAOIssue){
        try {
            testPublicKey(addressAOIssue.getDataKey());
        } catch (IOException | InvalidCipherTextException e) {
            logger.error("Unable to execute RSA", e);
            throw ApiExceptionFactory.exception(HttpStatus.UNPROCESSABLE_ENTITY, FAILED_ISSUE_MSG);
        }

        String reqAddress;
        try {
            reqAddress = addressFromKey(addressAOIssue.getSignKey());
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unable to execute SHA3-256", e);
            throw ApiExceptionFactory.exception(HttpStatus.UNPROCESSABLE_ENTITY, FAILED_ISSUE_MSG);
        }

        Optional<AddressDO> alreadyExists = addressRepository.findByAddress(reqAddress);
        if(alreadyExists.isPresent()) {
            logger.warn("Trying to issue a duplicate address");
            throw ApiExceptionFactory.exception(HttpStatus.UNPROCESSABLE_ENTITY, FAILED_ISSUE_MSG);
        }

        AddressDO addressDO = new AddressDO();
        addressDO.setAddress(reqAddress);
        addressDO.setDataKey(addressAOIssue.getDataKey());
        addressDO.setSignKey(addressAOIssue.getSignKey());
        addressDO.setIssued(ZonedDateTime.now(ZoneOffset.UTC));
        AddressDO savedAddressDO = addressRepository.save(addressDO);

        AddressAORsp addressAORsp = new AddressAORsp();
        addressAORsp.setAddress(savedAddressDO.getAddress());
        addressAORsp.setIssued(savedAddressDO.getIssued());
        return addressAORsp;
    }

    public Optional<AddressDO> getByAddress(String address){
        return addressRepository.findByAddress(address);
    }

    private String addressFromKey(String publicKey) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        byte[] bytes = md.digest(publicKey.getBytes(StandardCharsets.UTF_8));
        byte[] truncated = Arrays.copyOfRange(bytes, 12, bytes.length);
        return new String(Hex.encodeHex(truncated));
    }

    private void testPublicKey(String publicKeyBase64) throws IOException, InvalidCipherTextException {
        AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(Base64.getDecoder().decode(publicKeyBase64));
        PKCS1Encoding rsaCipher = new org.bouncycastle.crypto.encodings.PKCS1Encoding(new RSAEngine());
        rsaCipher.init(true, publicKey);
        String randomText = UUID.randomUUID().toString();
        rsaCipher.processBlock(randomText.getBytes(StandardCharsets.UTF_8), 0, randomText.length());
    }
}
