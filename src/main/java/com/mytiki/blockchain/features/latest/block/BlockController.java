/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.block;

import com.mytiki.common.ApiConstants;
import com.mytiki.common.reply.ApiReplyAO;
import com.mytiki.common.reply.ApiReplyAOFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = BlockController.PATH_CONTROLLER)
public class BlockController {

    public static final String PATH_CONTROLLER = ApiConstants.API_LATEST_ROUTE + "block";

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ApiReplyAO<BlockAORsp> postRefresh(@RequestBody BlockAOWrite body){
        return ApiReplyAOFactory.ok(blockService.write(body));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ApiReplyAO<List<BlockAORsp>> getBlock(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String hash
    ){
        return ApiReplyAOFactory.ok(blockService.find(hash, address));
    }
}
