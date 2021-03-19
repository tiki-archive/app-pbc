/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.main;

import com.mytiki.blockchain.config.ConfigBlockchainApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({
        ConfigBlockchainApp.class
})
@SpringBootApplication
public class BlockchainApp {

    public static void main(final String... args) {
        SpringApplication.run(BlockchainApp.class, args);
    }
}
