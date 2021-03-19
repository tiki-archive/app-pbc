/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.address;

import com.mytiki.common.exception.ApiExceptionFactory;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AddressRepository addressRepository;

    private static final String FAILED_ISSUE_MSG = "Failed to issue address";
    private static final String INVALID_SIGNATURE_MSG = "Invalid signature";

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public AddressAORsp issue(AddressAOIssue addressAOIssue){
        try {
            if(!validateSignature(addressAOIssue.getPublicKey(), addressAOIssue.getSignature()))
                throw ApiExceptionFactory.exception(HttpStatus.BAD_REQUEST, INVALID_SIGNATURE_MSG);
        } catch (InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            logger.error("Unable to execute ECDSA", e);
            throw ApiExceptionFactory.exception(HttpStatus.BAD_REQUEST, INVALID_SIGNATURE_MSG);
        } catch (NoSuchAlgorithmException e){
            logger.error("Unable to execute ECDSA", e);
            throw ApiExceptionFactory.exception(HttpStatus.UNPROCESSABLE_ENTITY, FAILED_ISSUE_MSG);
        }

        String reqAddress;
        try {
            reqAddress = addressFromKey(addressAOIssue.getPublicKey());
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unable to execute SHA-512", e);
            throw ApiExceptionFactory.exception(HttpStatus.UNPROCESSABLE_ENTITY, FAILED_ISSUE_MSG);
        }

        Optional<AddressDO> alreadyExists = addressRepository.findByAddress(reqAddress);
        if(alreadyExists.isPresent()) {
            logger.warn("Trying to issue a duplicate address");
            throw ApiExceptionFactory.exception(HttpStatus.UNPROCESSABLE_ENTITY, FAILED_ISSUE_MSG);
        }

        AddressDO addressDO = new AddressDO();
        addressDO.setAddress(reqAddress);
        addressDO.setPublicKey(addressAOIssue.getPublicKey());
        addressDO.setIssued(ZonedDateTime.now(ZoneOffset.UTC));
        AddressDO savedAddressDO = addressRepository.save(addressDO);

        AddressAORsp addressAORsp = new AddressAORsp();
        addressAORsp.setAddress(savedAddressDO.getAddress());
        addressAORsp.setIssued(savedAddressDO.getIssued());
        return addressAORsp;
    }

    private String addressFromKey(String publicKey) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        byte[] bytes = md.digest(publicKey.getBytes(StandardCharsets.UTF_8));
        byte[] truncated = Arrays.copyOfRange(bytes, 12, bytes.length);
        return new String(Hex.encodeHex(truncated));
    }

    private boolean validateSignature(String publicKeyBase64, String signature)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64));
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(publicKeyBase64.getBytes(StandardCharsets.UTF_8));
        return ecdsaVerify.verify(Base64.getDecoder().decode(signature));
    }
}
