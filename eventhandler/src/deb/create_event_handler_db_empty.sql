CREATE DATABASE  IF NOT EXISTS `arrowhead` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `arrowhead`;
-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: localhost    Database: event_handler
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
-- Table structure for table `event_filter`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `event_filter` (
  `id` int(11) NOT NULL,
  `end_date` datetime DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  `match_metadata` char(1) DEFAULT NULL,
  `notify_uri` varchar(255) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `consumer_system_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbkos27fkducgbn6rxqty2k6n1` (`event_type`,`consumer_system_id`),
  KEY `FK8k1vieqrr0cxw4x0ubocsrrpo` (`consumer_system_id`),
  CONSTRAINT `FK8k1vieqrr0cxw4x0ubocsrrpo` FOREIGN KEY (`consumer_system_id`) REFERENCES `arrowhead_system` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `event_filter_metadata`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `event_filter_metadata` (
  `filter_id` int(11) NOT NULL,
  `metadata_value` varchar(2047) DEFAULT NULL,
  `metadata_key` varchar(255) NOT NULL,
  PRIMARY KEY (`filter_id`,`metadata_key`),
  CONSTRAINT `FK1iu2vhxo8211io6weiwryguib` FOREIGN KEY (`filter_id`) REFERENCES `event_filter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `event_filter_sources_list`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `event_filter_sources_list` (
  `filter_id` int(11) NOT NULL,
  `sources_id` int(11) NOT NULL,
  UNIQUE KEY `UK_nbe4wrcv5w6rga8uc6t0cb0ck` (`sources_id`),
  KEY `FKqihrii4ab12xo3oxp5d5pb77j` (`filter_id`),
  CONSTRAINT `FK7gulo44n997tr1146xxi2xhfe` FOREIGN KEY (`sources_id`) REFERENCES `arrowhead_system` (`id`),
  CONSTRAINT `FKqihrii4ab12xo3oxp5d5pb77j` FOREIGN KEY (`filter_id`) REFERENCES `event_filter` (`id`)
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

-- Dump completed on 2018-05-24 13:44:00
