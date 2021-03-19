/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.address;

import com.mytiki.common.ApiConstants;
import com.mytiki.common.reply.ApiReplyAO;
import com.mytiki.common.reply.ApiReplyAOFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = AddressController.PATH_CONTROLLER)
public class AddressController {

    public static final String PATH_CONTROLLER = ApiConstants.API_LATEST_ROUTE + "address";
    public static final String PATH_ISSUE = "/issue";

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @RequestMapping(method = RequestMethod.POST, path = PATH_ISSUE)
    public ApiReplyAO<AddressAORsp> postRefresh(@RequestBody AddressAOIssue body){
        return ApiReplyAOFactory.ok(addressService.issue(body));
    }
}