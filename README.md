# GPCFacade

## Background
This repo contains the source code to build a Java Servlet WAR that does the following:

* Receives a [FHIR RESTful](https://www.hl7.org/fhir/http.html) search request for [MedicationRequest](https://www.hl7.org/fhir/medicationrequest.html) resources for a given NHS Patient.
* Makes a GP Connect [$gpc.getstructuredrecord](https://digital.nhs.uk/developer/api-catalogue/gp-connect-access-record-structured-fhir) request for the specified Patient (to the [GP Connect test service](https://orange.testlab.nhs.uk/)).
* Pulls out MedicationRequest and Medication resources from the returned Structured Record bundle.
* Converts those resources from [FHIR version](http://hl7.org/fhir/directory.html) STU3 to FHIR R4.
* Returns the converted resources as a searchset FHIR R4 Bundle.

Example query:
`http://[WAR Servlet host URL]/MedicationRequest?patient=https://fhir.nhs.uk/Id/nhs-number%7f9690937316`
