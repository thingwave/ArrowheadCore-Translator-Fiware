# Arrowhead_Transparency

To run, simply run the eclipse project. No arguments or configuration required (at this stage)

Using Postman on Ubuntu 18.04

Usage example to obtain a new translation:

POST http://localhost:8000/translator
Content-Type: application/xml
```xml
<translatorSetup>
<providerName>http</providerName>
<providerType>http</providerType>
<providerAddress>http://130.240.172.50:7000</providerAddress>
<consumerName>coap</consumerName>
<consumerType>coap</consumerType>
<consumerAddress>130.240.172.50</consumerAddress>
</translatorSetup>
```
 -- response --
200 OK
Content-Type:  text/html
Content-Length:  93
Server:  Jetty(9.1.0.M0)
```xml
<translationendpoint><id>14076</id><ip>130.240.172.50</ip><port>64736</port></translationendpoint>
```

Example to obtain all translations actives:

GET http://localhost:8000/translator

 -- response --
200 OK
Content-Type:  application/xml
Content-Length:  142
Server:  Jetty(9.1.0.M0)

Content-Type: application/xml
```xml
<translatorList>
    <translatorId>-14076</translatorId>
    <translatorAddress>coap://130.240.172.50:64736/</translatorAddress>
</translatorList>
```

Tested on localhost. For providerAddress and consumerAddress an absolut path is necessary

