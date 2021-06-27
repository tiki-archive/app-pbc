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
    data_key TEXT NOT NULL,
    sign_key TEXT NOT NULL,
    issued_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    short_code TEXT,
    PRIMARY KEY(id)
);

-- -----------------------------------------------------------------------
-- BLOCK
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS block(
    id BIGSERIAL NOT NULL,
    hash TEXT NOT NULL UNIQUE,
    previous_hash TEXT NOT NULL,
    address TEXT NOT NULL REFERENCES address (address),
    created_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    data TEXT NOT NULL,
    PRIMARY KEY(id)
);