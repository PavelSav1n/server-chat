# server-chat application

## contents

## SQL SETUP


1. First of all you will need to create a `schema` for tables where we will be storing all data about users & messages. If you want to use schema of your own, you will have to change prepaired query code in MessageDaoImpl.java & UserDaoImpl.java 
```SQL
CREATE SCHEMA schema_server ;
```

```SQL
DROP TABLE IF EXISTS `schema_server.chat_messages`;
DROP TABLE IF EXISTS `schema_server.chat_users`;
```

```SQL
CREATE TABLE `chat_messages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sender` int NOT NULL,
  `recipient` int DEFAULT NULL,
  `text` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idnew_table_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

```SQL
CREATE TABLE `chat_users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `login` varchar(256) NOT NULL,
  `password` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `login_UNIQUE` (`login`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

