/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

-- -----------------------------------------------------------------------
-- ADDRESS
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS address(
    id BIGSERIAL NOT NULL,
    address TEXT NOT NULL UNIQUE,
    public_key TEXT NOT NULL,
    issued_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY(id)
);

-- -----------------------------------------------------------------------
-- BLOCK
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS block(
    id BIGSERIAL NOT NULL,
    hash TEXT NOT NULL UNIQUE,
    previous_hash TEXT NOT NULL,
    address BIGINT NOT NULL REFERENCES address (id),
    signature TEXT NOT NULL,
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    data TEXT NOT NULL,
    PRIMARY KEY(id)
);