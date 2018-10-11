CREATE DATABASE  IF NOT EXISTS `arrowhead`;
USE `arrowhead`;

CREATE TABLE  IF NOT EXISTS `own_cloud` (
  `cloud_id` int(11) NOT NULL,
  PRIMARY KEY (`cloud_id`),
  CONSTRAINT `FKr3avkpkrx88jt4atfmxewqkl8` FOREIGN KEY (`cloud_id`) REFERENCES `arrowhead_cloud` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;