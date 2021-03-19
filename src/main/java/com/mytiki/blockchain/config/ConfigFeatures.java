/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.config;

import com.mytiki.blockchain.features.latest.address.AddressConfig;
import com.mytiki.blockchain.features.latest.block.BlockConfig;
import org.springframework.context.annotation.Import;

@Import({
        AddressConfig.class,
        BlockConfig.class
})
public class ConfigFeatures {
}
