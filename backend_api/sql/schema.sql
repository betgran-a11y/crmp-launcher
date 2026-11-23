CREATE DATABASE crmp_db;
\c crmp_db;

CREATE TABLE IF NOT EXISTS accounts (
    id SERIAL PRIMARY KEY,
    nickname VARCHAR(24) UNIQUE NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    referral_nick VARCHAR(24),
    is_banned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS characters (
    id SERIAL PRIMARY KEY,
    account_id INT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    gender SMALLINT NOT NULL DEFAULT 1,
    skin_id INT NOT NULL DEFAULT 1,
    cash BIGINT NOT NULL DEFAULT 5000,
    bank_balance BIGINT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    exp INT NOT NULL DEFAULT 0,
    job_id VARCHAR(32) NOT NULL DEFAULT 'unemployed',
    faction_id INT DEFAULT NULL,
    faction_rank SMALLINT NOT NULL DEFAULT 0,
    wanted_stars SMALLINT NOT NULL DEFAULT 0,
    jail_seconds INT NOT NULL DEFAULT 0,
    pos_x REAL NOT NULL DEFAULT 0.0,
    pos_y REAL NOT NULL DEFAULT 2.0,
    pos_z REAL NOT NULL DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    owner_char_id INT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    model_code VARCHAR(32) NOT NULL, -- e.g. vaz_2109, gaz_3102, uaz_469
    plate_number VARCHAR(16) UNIQUE NOT NULL,
    color_hex CHAR(7) NOT NULL DEFAULT '#7A7A7A',
    engine_lvl SMALLINT NOT NULL DEFAULT 1,
    suspension_lvl SMALLINT NOT NULL DEFAULT 1,
    brakes_lvl SMALLINT NOT NULL DEFAULT 1,
    tint_percent SMALLINT NOT NULL DEFAULT 0,
    health REAL NOT NULL DEFAULT 1000.0,
    impounded BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS properties (
    id SERIAL PRIMARY KEY,
    owner_char_id INT REFERENCES characters(id) ON DELETE SET NULL,
    district VARCHAR(48) NOT NULL, -- center, panel_district, industrial, private_sector
    property_type VARCHAR(24) NOT NULL, -- apartment, house, garage, commercial
    price BIGINT NOT NULL,
    tax_per_day INT NOT NULL DEFAULT 250
);

CREATE TABLE IF NOT EXISTS businesses (
    id SERIAL PRIMARY KEY,
    owner_char_id INT REFERENCES characters(id) ON DELETE SET NULL,
    name VARCHAR(64) NOT NULL,
    biz_type VARCHAR(32) NOT NULL, -- auto_service, car_wash, shop, club, gas_station, dealership, shooting_range
    balance BIGINT NOT NULL DEFAULT 0,
    stock_units INT NOT NULL DEFAULT 100,
    tax_debt BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS game_servers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    host VARCHAR(64) NOT NULL,
    port INT NOT NULL DEFAULT 7777,
    online_count INT NOT NULL DEFAULT 0,
    max_players INT NOT NULL DEFAULT 300,
    status VARCHAR(16) NOT NULL DEFAULT 'online'
);

INSERT INTO game_servers (name, host, port, online_count, max_players, status)
VALUES ('Сервер #1 | Северный', '127.0.0.1', 7777, 42, 300, 'online')
ON CONFLICT DO NOTHING;
