# server-chat application

## contents
0. [About](https://github.com/PavelSav1n/server-chat#0-about)
1. [SQL setup](https://github.com/PavelSav1n/server-chat#1-sql-setup)
2. [application.properties preparaion](https://github.com/PavelSav1n/server-chat#2-applicationproperties-preparaion)
3. [Other settings](https://github.com/PavelSav1n/server-chat#3-other-settings)


## 0. About

`Client-server chat application` project was developed as a final task of ITsJAVA Java Developer course. The aim was to develop a client-server chat application with following features:
- Client-server architecture
- Observer pattern implementation
- SQL database integration
- Multiple users
- Authorisation/registration process
- Password encryption
- Logging
- Saving data to file
- Implementation of some commands, like private messaging & message history
 
 Time elapsed: approximatly 2 weeks.
 
 Full project `Client-server chat application` concists of two parts:
- [server-chat application](https://github.com/PavelSav1n/server-chat)
- [client-chat](https://github.com/PavelSav1n/client-chat)

## 1. SQL setup

1.1. This application requires MySQL server, or any other [RDBMS](https://en.wikipedia.org/wiki/Relational_database#RDBMS). How to install and setup MySQL server on Windows you can see [this](https://youtu.be/u96rVINbAUI) video. Here you can download [MySQL Installer 8.0.32](https://dev.mysql.com/get/Downloads/MySQLInstaller/mysql-installer-community-8.0.32.0.msi)

1.2. After you've finished with MySQL server setup, first of all you will need to create a `schema` for tables where we will be storing all data about users & messages. If you want to use `schema` of your own, you will have to change prepaired query code in `MessageDaoImpl.java` & `UserDaoImpl.java` 
```SQL
CREATE SCHEMA schema_server ;
```

1.3. (OPTIONAL) If you already have tables with equal names `chat_messages` & `chat_users` in your schema, it would be better to drop them, to make sure they meet requirements:
```SQL
DROP TABLE IF EXISTS `schema_server.chat_messages`;
DROP TABLE IF EXISTS `schema_server.chat_users`;
```

1.4. Now you must create `chat_messages` table:
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
and `chat_users` table:
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

## 2. application.properties preparaion

Once you've finished SQL setup, it's time to set up [application.properties](https://github.com/PavelSav1n/server-chat/blob/master/src/main/resources/application.properties)

`db.url` -- is for MySQL server address. If localhost, then keep default settings.

`db.login` and `db.password` -- is for SQL database login & password respectively.

```C#
db.url=jdbc:MySql://localhost:3306/schema_online_course?serverTimezone=UTC
db.login=yourLoginHere
db.password=yourPasswordHere
```

## 3. Other settings

3.1. For application to run properly, you must open default port `3306` in your firewall. At the first start of application Windows will ask whether or not permit the access for application to port `3306`

3.2. `server-chat application` has different layers of logging, which you can change in [log4j.properties](https://github.com/PavelSav1n/server-chat/blob/master/src/main/resources/log4j.properties). Some basic information about how to setup Log4j is present in those properties via comments.

:arrow_up_small:[Back to contents](https://github.com/PavelSav1n/server-chat#contents)
