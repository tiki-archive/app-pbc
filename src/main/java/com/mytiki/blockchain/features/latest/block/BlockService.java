/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.block;

import com.mytiki.blockchain.features.latest.address.AddressDO;
import com.mytiki.blockchain.features.latest.address.AddressService;
import com.mytiki.common.exception.ApiExceptionFactory;
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
import java.util.*;
import java.util.stream.Collectors;

public class BlockService {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BlockRepository blockRepository;
    private final AddressService addressService;

    private static final String INVALID_ADDRESS_MSG = "Blockchain address is invalid";
    private static final String INVALID_SIGNATURE_MSG = "Failed to validate signature";
    private static final String FAILED_WRITE_MSG = "Failed to write to blockchain";
    private static final String MISSING_PARAM_MSG = "A filter parameter is required";

    public BlockService(BlockRepository blockRepository, AddressService addressService) {
        this.blockRepository = blockRepository;
        this.addressService = addressService;
    }

    public BlockAORsp write(BlockAOWrite blockAOWrite){
        Optional<AddressDO> addressDO = addressService.getByAddress(blockAOWrite.getAddress());
        if(addressDO.isEmpty())
            throw ApiExceptionFactory.exception(HttpStatus.BAD_REQUEST, INVALID_ADDRESS_MSG);

        try{
            if(!validateSignature(addressDO.get().getSignKey(), blockAOWrite))
                throw ApiExceptionFactory.exception(HttpStatus.BAD_REQUEST, INVALID_SIGNATURE_MSG);
        }catch (InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            throw ApiExceptionFactory.exception(HttpStatus.BAD_REQUEST, INVALID_SIGNATURE_MSG);
        } catch (NoSuchAlgorithmException e){
            logger.error("Unable to execute ECDSA", e);
            throw ApiExceptionFactory.exception(HttpStatus.UNPROCESSABLE_ENTITY, FAILED_WRITE_MSG);
        }

        BlockDO blockDO = new BlockDO();
        blockDO.setAddress(blockAOWrite.getAddress());
        blockDO.setData(blockAOWrite.getData());
        blockDO.setHash("TMP." + UUID.randomUUID().toString());
        blockDO.setPreviousHash("TMP." + UUID.randomUUID().toString());
        blockDO.setCreated(ZonedDateTime.now(ZoneOffset.UTC));
        BlockDO savedBlockDO = blockRepository.save(blockDO);

        savedBlockDO.setPreviousHash(getPrevious(savedBlockDO.getId()).getHash());
        try {
            savedBlockDO.setHash(createHash(
                    savedBlockDO.getPreviousHash(),
                    savedBlockDO.getCreated(),
                    savedBlockDO.getData())
            );
        }catch (NoSuchAlgorithmException e) {
            logger.error("Failed to create block hash ", e);
            throw ApiExceptionFactory.exception(HttpStatus.UNPROCESSABLE_ENTITY, FAILED_WRITE_MSG);
        }
        savedBlockDO = blockRepository.save(blockDO);

        return blockDOtoAORsp(savedBlockDO);
    }

    public List<BlockAORsp> find(String hash, String address){
        if(hash != null && !hash.isBlank()){
            Optional<BlockDO> blockDO = blockRepository.findByHash(hash);
            if(blockDO.isEmpty()) return new ArrayList<>(0);
            return Collections.singletonList(blockDOtoAORsp(blockDO.get()));
        }else if(address != null && !address.isBlank()){
            List<BlockDO> blockDOs = blockRepository.findAllByAddress(address);
            return blockDOs.stream().map(this::blockDOtoAORsp).collect(Collectors.toList());
        }else
            throw ApiExceptionFactory.exception(HttpStatus.BAD_REQUEST, MISSING_PARAM_MSG);
    }

    private boolean validateSignature(String publicKeyBase64, BlockAOWrite blockAOWrite)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        String validate = blockAOWrite.getAddress() + blockAOWrite.getData();
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64));
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(validate.getBytes(StandardCharsets.UTF_8));
        return ecdsaVerify.verify(Base64.getDecoder().decode(blockAOWrite.getSignature()));
    }

    private String createHash(String previousHash, ZonedDateTime created, String data)
            throws NoSuchAlgorithmException {
        String fingerprint = previousHash + created.toOffsetDateTime().toString() + data;
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        byte[] bytes = md.digest(fingerprint.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(bytes);
    }

    private BlockDO getPrevious(Long id){
        Optional<BlockDO> previousBlockDO = blockRepository.findById(id-1);
        if(previousBlockDO.isEmpty()) {
            logger.error("Tried to add block {} but could not find previous block {}. Deleting", id, id-1 );
            blockRepository.deleteById(id);
            throw ApiExceptionFactory.exception(HttpStatus.UNPROCESSABLE_ENTITY, FAILED_WRITE_MSG);
        }
        if(previousBlockDO.get().getPreviousHash().contains("TMP.")){
            logger.error("Theres a bad block in the chain. Moving up");
            return getPrevious(previousBlockDO.get().getId());
        }else
            return previousBlockDO.get();
    }

    private BlockAORsp blockDOtoAORsp(BlockDO blockDO){
        BlockAORsp blockAORsp = new BlockAORsp();
        blockAORsp.setHash(blockDO.getHash());
        blockAORsp.setCreated(blockDO.getCreated());
        blockAORsp.setPreviousHash(blockDO.getPreviousHash());
        blockAORsp.setData(blockDO.getData());
        blockAORsp.setAddress(blockDO.getAddress());
        return blockAORsp;
    }
}
