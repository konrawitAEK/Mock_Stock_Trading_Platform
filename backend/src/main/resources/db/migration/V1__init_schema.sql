CREATE TABLE stocks (
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

CREATE TABLE portfolio_items (
    symbol        VARCHAR(10)   NOT NULL,
    quantity      INT           NOT NULL,
    avg_buy_price DECIMAL(12,4) NOT NULL,
    CONSTRAINT pk_portfolio_items PRIMARY KEY (symbol)
);

CREATE TABLE transactions (
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

CREATE INDEX idx_transactions_timestamp ON transactions (timestamp DESC);

CREATE TABLE user_state (
    id   BIGINT        NOT NULL,
    cash DECIMAL(15,4) NOT NULL,
    CONSTRAINT pk_user_state PRIMARY KEY (id)
);
