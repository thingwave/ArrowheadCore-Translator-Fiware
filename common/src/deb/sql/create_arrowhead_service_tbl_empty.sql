CREATE DATABASE  IF NOT EXISTS `arrowhead`;
USE `arrowhead`;

CREATE TABLE  IF NOT EXISTS `arrowhead_service` (
  `id` int(11) NOT NULL,
  `service_definition` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg90gjpqpv7tpmy1eou5u4umyk` (`service_definition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;