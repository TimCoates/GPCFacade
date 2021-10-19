# GPCFacade

## Background
GP Connect uses a FHIR RPC pattern to retrieve a Patient's medical record. There is a desire to:
* Provide a simpler RESTful API
* Provide the API in FHIR R4

This project is a prototype / Proof of Concept to illustrate how this might be achievable.

**NB:**

**THIS IS NOT USING REAL PATIENT DATA**

**THIS IS TESTED ONLY TO  THE EXTENT THAT IT 'SEEMS TO' WORK**

**THIS IS PROVIDED WIH NO WARRANTY OR GUARANTEE**
## Details
This repo contains the source code to build a Java Servlet WAR that does the following:
* Receives a [FHIR RESTful](https://www.hl7.org/fhir/http.html) search request for [MedicationRequest](https://www.hl7.org/fhir/medicationrequest.html) resources for a given NHS Patient.
* Makes a GP Connect [$gpc.getstructuredrecord](https://digital.nhs.uk/developer/api-catalogue/gp-connect-access-record-structured-fhir) request for the specified Patient (to the [GP Connect test service](https://orange.testlab.nhs.uk/)).
* Pulls out MedicationRequest and Medication resources from the returned Structured Record bundle.
* Converts those resources from [FHIR version](http://hl7.org/fhir/directory.html) STU3 to FHIR R4.
* Updates the [MedicationRequest.subject](https://www.hl7.org/fhir/medicationrequest-definitions.html#MedicationRequest.subject) field to point to the [PDS](https://digital.nhs.uk/developer/api-catalogue/personal-demographics-service-fhir) resource representing the Patient.
* Returns the converted resources as a searchset FHIR R4 Bundle.

## Usage
Example query:
`http://[WAR Servlet host URL]/MedicationRequest?patient=https://fhir.nhs.uk/Id/nhs-number%7f9690937316`


## OpenAPI
When run, the WAR provides OpenAPI 3.0 Documentation of the server at:

`http://[WAR Servlet host URL]/api-docs`.

A [Swagger UI](https://swagger.io/tools/swagger-ui/) tool served at:

`http://[WAR Servlet host URL]/swagger-ui/`


## To do:
* Add tests.
* Clean up the messy code!
* Remove spurious logging added during initial build / debugging.
* Reuse FHIR Contexts, Parsers etc to optimise performance.
* Expand to cover [MedicationStatement](https://www.hl7.org/fhir/medicationstatement.html#MedicationStatement) resources.
* Look at other referenced Resources (e.g. Practitioner and Organization) in MedicationRequest to see whether they can / should be included / dropped / changed.


### Notes
* The resulting WAR does not appear to work on a [Glassfish](https://javaee.github.io/glassfish/) server, the Sun classes used for https calls throw errors.
