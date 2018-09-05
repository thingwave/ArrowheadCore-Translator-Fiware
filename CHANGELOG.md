### September 5th, 2018
* AccessControlFilters now have an abstract parent class to avoid duplicate codes
* Added 2 different certificate signing method to the CA module, both of which still need thorough testing

### August 27th, 2018
* Fixed the registered packages on web server startup
* ArrowheadCloud and ArrowheadSystem names now allow for the following special characters too: _ - :
* Added custom exception to certificate validation path related errors, when sending request to other systems
* ServiceRegistryEntry contains a String metadata field again (With the current security metadata, this is needed to be able to provide a service 
in secure and insecure mode at the same time)
* Fixed a bug in Gatekeeper AccessControlFilter, preventing GSD/ICN process in secure mode
* Secure and insecure mode of the full framework can now work at the same time with the same database, if the ArrowheadCloud (and OwnCloud + 
NeighborCloud) table contains both Gatekeeper versions.

### August 13th, 2018
* Our complicated custom input validation solution replaced with a clean, easy to use annotation based validation library (Hibernate validator)
* Management API refactorings, to provide ID-field based resource query, update and delete operations
* Common resources (services, systems, clouds, devices) have their management REST endpoints in the common module now, so each module can provide their own endpoints for their database
* Complex entity classes now use Hibernate's @OnDelete annotation to delete child entities, when a parent entity gets deleted
* Created new common resource: ArrowheadDevice
* Service metadata is moved back to ArrowheadService, and the port field is moved back to ArrowheadSystem (from ServiceRegistryEntry)