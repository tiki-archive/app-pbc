/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.address;

import com.mytiki.common.ApiConstants;
import com.mytiki.common.reply.ApiReplyAO;
import com.mytiki.common.reply.ApiReplyAOFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = AddressController.PATH_CONTROLLER)
public class AddressController {

    public static final String PATH_CONTROLLER = ApiConstants.API_LATEST_ROUTE + "address";
    public static final String PATH_ISSUE = "/issue";
    @Deprecated
    public static final String PATH_REFER = "/refer";
    public static final String PATH_CODE = "/code";

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @RequestMapping(method = RequestMethod.POST, path = PATH_ISSUE)
    public ApiReplyAO<AddressAOIssueRsp> postRefresh(@RequestBody AddressAOIssue body){
        return ApiReplyAOFactory.ok(addressService.issue(body));
    }

    @Deprecated
    @RequestMapping(method = RequestMethod.GET, path = PATH_REFER + "/{address}/count")
    public ApiReplyAO<AddressAOReferRsp> getCount(@PathVariable String address){
        return ApiReplyAOFactory.ok(addressService.getReferCount(address));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{address}" + PATH_CODE)
    public ApiReplyAO<AddressAOCodeRsp> getShortCode(@PathVariable String address){
        return ApiReplyAOFactory.ok(addressService.getShortCode(address));
    }
}
