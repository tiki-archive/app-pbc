/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.block;

import com.mytiki.blockchain.features.latest.address.AddressService;
import com.mytiki.blockchain.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(BlockConfig.PACKAGE_PATH)
@EntityScan(BlockConfig.PACKAGE_PATH)
public class BlockConfig {

    public static final String PACKAGE_PATH = Constants.PACKAGE_FEATURES_LATEST_DOT_PATH + ".block";

    @Bean
    public BlockController blockController(@Autowired BlockService blockService){
        return new BlockController(blockService);
    }

    @Bean
    public BlockService blockService(
            @Autowired BlockRepository blockRepository,
            @Autowired AddressService addressService
    ){
        return new BlockService(blockRepository, addressService);
    }
}
