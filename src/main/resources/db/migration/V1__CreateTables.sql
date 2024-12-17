CREATE TABLE IF NOT EXISTS files (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     file_name TEXT NOT NULL,
     download_url TEXT NOT NULL,
     upload_time INTEGER NOT NULL,
     size TEXT NOT NULL,
     file_id TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS configs (
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   name TEXT NOT NULL,
   token TEXT NOT NULL,
   target INTEGER NOT NULL,
   url INTEGER,
   pass TEXT
);

CREATE TABLE IF NOT EXISTS users (
     id INTEGER PRIMARY KEY AUTOINCREMENT,         -- 用户ID，主键，自增
     username TEXT NOT NULL UNIQUE,               -- 用户名，唯一
     password TEXT NOT NULL,                     -- 加密的用户密码
     role TEXT DEFAULT 'user',                  -- 角色，如 admin、user 等
     reserved_1 TEXT DEFAULT NULL,             -- 保留字段1，便于以后扩展
     reserved_2 TEXT DEFAULT NULL,             -- 保留字段2，便于以后扩展
     reserved_3 TEXT DEFAULT NULL              -- 保留字段3，便于以后扩展
);