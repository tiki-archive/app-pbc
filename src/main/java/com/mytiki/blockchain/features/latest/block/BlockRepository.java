/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.blockchain.features.latest.block;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<BlockDO, Long> {

    Optional<BlockDO> findByHash(String hash);
    List<BlockDO> findAllByAddress(String address);
}
