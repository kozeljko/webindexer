CREATE SCHEMA IF NOT EXISTS indexer;

CREATE TABLE IF NOT EXISTS indexer.IndexWord
(
    word TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS indexer."Posting"
(
    word         TEXT    NOT NULL,
    document_name TEXT    NOT NULL,
    frequency    INTEGER NOT NULL,
    indexes      TEXT    NOT NULL,
    PRIMARY KEY (word, document_name),
    FOREIGN KEY (word) REFERENCES IndexWord (word)
);