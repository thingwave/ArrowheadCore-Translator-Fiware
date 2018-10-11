CREATE DATABASE  IF NOT EXISTS `arrowhead` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `arrowhead`;
-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: localhost    Database: orchestrator
-- ------------------------------------------------------
-- Server version	5.7.21-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `orchestration_store`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `orchestration_store` (
  `id` int(11) NOT NULL,
  `is_default` char(1) DEFAULT NULL,
  `instruction` varchar(255) DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  `consumer_system_id` int(11) DEFAULT NULL,
  `provider_cloud_id` int(11) DEFAULT NULL,
  `provider_system_id` int(11) DEFAULT NULL,
  `arrowhead_service_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK328vwkn9l8phjq4j276wb13w9` (`arrowhead_service_id`,`consumer_system_id`,`priority`,`is_default`),
  KEY `FKg9jtg1go2yety7s6qimnbqdtc` (`consumer_system_id`),
  KEY `FK4as8nlx9s4a6a9r6y4oswj5do` (`provider_cloud_id`),
  KEY `FK1a9yusgvqs0jrna2y8cgdeusb` (`provider_system_id`),
  CONSTRAINT `FK1a9yusgvqs0jrna2y8cgdeusb` FOREIGN KEY (`provider_system_id`) REFERENCES `arrowhead_system` (`id`),
  CONSTRAINT `FK4as8nlx9s4a6a9r6y4oswj5do` FOREIGN KEY (`provider_cloud_id`) REFERENCES `arrowhead_cloud` (`id`),
  CONSTRAINT `FKg9jtg1go2yety7s6qimnbqdtc` FOREIGN KEY (`consumer_system_id`) REFERENCES `arrowhead_system` (`id`),
  CONSTRAINT `FKnjr4mytp6bipwyc9sv9y1ip51` FOREIGN KEY (`arrowhead_service_id`) REFERENCES `arrowhead_service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orchestration_store_attributes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `orchestration_store_attributes` (
  `store_entry_id` int(11) NOT NULL,
  `attribute_value` varchar(2047) DEFAULT NULL,
  `attribute_key` varchar(255) NOT NULL,
  PRIMARY KEY (`store_entry_id`,`attribute_key`),
  CONSTRAINT `FKrtqe93seoude4elrqmk1qdowj` FOREIGN KEY (`store_entry_id`) REFERENCES `orchestration_store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-05-24 13:44:51
