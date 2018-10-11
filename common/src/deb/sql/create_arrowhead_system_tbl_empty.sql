CREATE DATABASE  IF NOT EXISTS `arrowhead`;
USE `arrowhead`;

CREATE TABLE  IF NOT EXISTS `arrowhead_system` (
  `id` int(11) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `system_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3rj1egf6gi1enagslqry0pkkl` (`system_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
