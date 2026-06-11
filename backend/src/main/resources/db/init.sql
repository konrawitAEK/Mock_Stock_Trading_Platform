-- Run this script once against a fresh PostgreSQL database before starting the application.
-- psql -U mockstock -d mockstock -f init.sql

-- Tables

CREATE TABLE IF NOT EXISTS stocks (
    symbol         VARCHAR(10)    NOT NULL,
    company_name   VARCHAR(255)   NOT NULL,
    current_price  DECIMAL(12,4)  NOT NULL,
    previous_price DECIMAL(12,4)  NOT NULL,
    daily_change   DECIMAL(12,4)  NOT NULL DEFAULT 0,
    change_percent DECIMAL(8,4)   NOT NULL DEFAULT 0,
    sector         VARCHAR(100)   NOT NULL,
    description    TEXT           NOT NULL,
    CONSTRAINT pk_stocks PRIMARY KEY (symbol)
);

CREATE TABLE IF NOT EXISTS portfolio_items (
    symbol        VARCHAR(10)   NOT NULL,
    quantity      INT           NOT NULL,
    avg_buy_price DECIMAL(12,4) NOT NULL,
    CONSTRAINT pk_portfolio_items PRIMARY KEY (symbol)
);

CREATE TABLE IF NOT EXISTS transactions (
    id           VARCHAR(36)   NOT NULL,
    timestamp    TIMESTAMP     NOT NULL,
    type         VARCHAR(4)    NOT NULL,
    symbol       VARCHAR(10)   NOT NULL,
    company_name VARCHAR(255)  NOT NULL,
    quantity     INT           NOT NULL,
    price        DECIMAL(12,4) NOT NULL,
    total_amount DECIMAL(15,4) NOT NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_transactions_timestamp ON transactions (timestamp DESC);

CREATE TABLE IF NOT EXISTS user_state (
    id   BIGINT        NOT NULL,
    cash DECIMAL(15,4) NOT NULL,
    CONSTRAINT pk_user_state PRIMARY KEY (id)
);

-- Seed data

INSERT INTO user_state (id, cash) VALUES (1, 100000.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO stocks (symbol, company_name, current_price, previous_price, daily_change, change_percent, sector, description) VALUES
('AAPL',  'Apple Inc.',             182.50, 182.50, 0, 0, 'Tech',
 'Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide. Known for the iPhone, Mac, iPad, and services ecosystem.'),
('TSLA',  'Tesla Inc.',             238.00, 238.00, 0, 0, 'Automotive',
 'Tesla, Inc. designs, develops, manufactures, and sells electric vehicles, energy generation, and storage systems. A pioneer in sustainable transport and clean energy solutions.'),
('MSFT',  'Microsoft Corp.',        375.20, 375.20, 0, 0, 'Tech',
 'Microsoft Corporation develops and supports software, services, devices, and solutions worldwide. Products include Windows, Office 365, Azure cloud platform, and Xbox gaming systems.'),
('GOOGL', 'Alphabet Inc.',          141.80, 141.80, 0, 0, 'Tech',
 'Alphabet Inc. provides online advertising services, search engine technology, cloud computing, software, and hardware products. Parent company of Google, YouTube, and DeepMind.'),
('AMZN',  'Amazon.com Inc.',        178.90, 178.90, 0, 0, 'E-Commerce',
 'Amazon.com, Inc. engages in the retail sale of consumer products, subscriptions, and web services. Operates AWS cloud platform and leads in global e-commerce and logistics.'),
('NVDA',  'NVIDIA Corp.',           495.00, 495.00, 0, 0, 'Semiconductors',
 'NVIDIA Corporation provides graphics and compute and networking solutions. Leader in GPU technology powering AI, gaming, data centers, and autonomous vehicles.'),
('META',  'Meta Platforms',         326.40, 326.40, 0, 0, 'Social Media',
 'Meta Platforms, Inc. develops products that enable people to connect and share through mobile devices, PCs, and other surfaces. Operates Facebook, Instagram, WhatsApp, and the metaverse platform.'),
('NFLX',  'Netflix Inc.',           445.60, 445.60, 0, 0, 'Entertainment',
 'Netflix, Inc. provides entertainment services worldwide. Offers TV series, documentaries, feature films, and mobile games across various genres and languages via streaming.'),
('AMD',   'Advanced Micro Devices', 168.30, 168.30, 0, 0, 'Semiconductors',
 'Advanced Micro Devices, Inc. operates as a semiconductor company worldwide. Designs and markets CPUs, GPUs, and data center solutions competing with Intel and NVIDIA.'),
('BABA',  'Alibaba Group',           85.70,  85.70, 0, 0, 'E-Commerce',
 'Alibaba Group Holding Limited provides technology infrastructure and marketing reach to merchants, brands, retailers, and other businesses globally. Operates Taobao, Tmall, and Alibaba Cloud.')
ON CONFLICT (symbol) DO NOTHING;
