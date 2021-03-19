/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.config;

import com.mytiki.blockchain.features.latest.address.AddressConfig;
import org.springframework.context.annotation.Import;

@Import({
        AddressConfig.class
})
public class ConfigFeatures {
}
