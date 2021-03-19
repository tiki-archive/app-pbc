/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.config;

import com.mytiki.blockchain.utilities.UtilitiesConfig;
import com.mytiki.common.exception.ApiExceptionHandlerDefault;
import com.mytiki.common.reply.ApiReplyHandlerDefault;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.security.Security;
import java.util.TimeZone;

@Import({
        ConfigProperties.class,
        ApiExceptionHandlerDefault.class,
        ApiReplyHandlerDefault.class,
        UtilitiesConfig.class,
        ConfigFeatures.class,
        ConfigSecurity.class
})
public class ConfigBlockchainApp {

    @PostConstruct
    void starter(){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }
}
