CREATE DATABASE  IF NOT EXISTS `arrowhead`;
USE `arrowhead`;

CREATE TABLE  IF NOT EXISTS `arrowhead_service_interface_list` (
  `arrowhead_service_id` int(11) NOT NULL,
  `interfaces` varchar(255) DEFAULT NULL,
  KEY `FKfaxi77ynuub343wunfiny2p0` (`arrowhead_service_id`),
  CONSTRAINT `FKfaxi77ynuub343wunfiny2p0` FOREIGN KEY (`arrowhead_service_id`) REFERENCES `arrowhead_service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
