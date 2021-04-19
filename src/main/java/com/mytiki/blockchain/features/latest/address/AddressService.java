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
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AddressRepository addressRepository;

    private static final String FAILED_ISSUE_MSG = "Failed to issue address";

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public AddressAOIssueRsp issue(AddressAOIssue addressAOIssue){
        if(!testDataKey(addressAOIssue.getDataKey()) || !testSignKey(addressAOIssue.getSignKey()))
            throw ApiExceptionFactory.exception(HttpStatus.BAD_REQUEST, FAILED_ISSUE_MSG);

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
        addressDO.setReferFrom(addressAOIssue.getReferFrom());
        AddressDO savedDO = addressRepository.save(addressDO);

        AddressAOIssueRsp addressAORsp = new AddressAOIssueRsp();
        addressAORsp.setAddress(savedDO.getAddress());
        addressAORsp.setIssued(savedDO.getIssued());
        return addressAORsp;
    }

    public Optional<AddressDO> getByAddress(String address){
        return addressRepository.findByAddress(address);
    }

    public AddressAOReferRsp getReferCount(String address) {
        List<AddressDO> addressDOList = addressRepository.findByReferFrom(address);
        AddressAOReferRsp rsp = new AddressAOReferRsp();
        rsp.setCount(addressDOList.size());
        return rsp;
    }

    private String addressFromKey(String publicKey) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        byte[] addressBytes = md.digest(publicKey.getBytes(StandardCharsets.UTF_8));
        return new String(Hex.encodeHex(addressBytes));
    }

    private boolean testDataKey(String dataKey) {
        try {
            AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(Base64.getDecoder().decode(dataKey));
            PKCS1Encoding rsaCipher = new org.bouncycastle.crypto.encodings.PKCS1Encoding(new RSAEngine());
            rsaCipher.init(true, publicKey);
            String randomText = UUID.randomUUID().toString();
            rsaCipher.processBlock(randomText.getBytes(StandardCharsets.UTF_8), 0, randomText.length());
            return true;
        }catch (IOException | InvalidCipherTextException e){
            logger.error("Unable to execute RSA", e);
            return false;
        }
    }

    private boolean testSignKey(String signKey) {
        try {
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(signKey));
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            ecdsaVerify.initVerify(publicKey);
            return true;
        }catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e){
            logger.error("Unable to execute ECDSA", e);
            return false;
        }
    }
}
