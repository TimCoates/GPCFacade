/*
 * Copyright (C) 2021 NHS Digital.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.justtim.gpcfacade.gpconnect;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;

/**
 *
 * @author tim.coates@nhs.net
 */
public class GetGPCRecord {

    private static final Logger LOG = Logger.getLogger(GetGPCRecord.class.getName());

    public GetGPCRecord() {
    }

    public org.hl7.fhir.r4.model.Bundle call(String NHSNumber) {
        LOG.info("Ready to call for NHS Number");
        // We're going to be returning a Bundle result set...
        org.hl7.fhir.r4.model.Bundle resultsList = new org.hl7.fhir.r4.model.Bundle();
        resultsList.setType(org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET);

        // Here we call GP Connect and get back their Bundle...
        FhirContext ctx = FhirContext.forDstu3();
        Bundle GPCRecord = actualCall(ctx, NHSNumber);
        LOG.info("GPCRecord had " + GPCRecord.getEntry().size() + " resources");

        // Here we're going to iterate over the resources...
        List<org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent> entries = GPCRecord.getEntry();
        Iterator<org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent> iterator = entries.iterator();
        List<BundleEntryComponent> newEntries = new ArrayList<BundleEntryComponent>();
        int total = 0;

        Reference subject = new Reference().setReference("https://api.service.nhs.uk/personal-demographics/FHIR/R4/Patient/" + NHSNumber);

        while (iterator.hasNext()) {
            org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry = iterator.next();
            ResourceType r = entry.getResource().getResourceType();

            LOG.info("Got resource: " + r.toString());
            if (r.toString().equals("MedicationRequest")) {
                LOG.info("Got a MedicationRequest");
                org.hl7.fhir.dstu3.model.MedicationRequest medicationRequest = (org.hl7.fhir.dstu3.model.MedicationRequest) entry.getResource();
                LOG.info("Converting to R4");
                MedicationRequest newOne = convertMedicationRequestToR4(ctx, medicationRequest);
                newOne.setSubject(subject);
                BundleEntryComponent newItem = new BundleEntryComponent().setResource(newOne);
                LOG.info("Adding to the list");
                newEntries.add(newItem);
                total++;
            }

            if (r.toString().equals("Medication")) {
                LOG.info("Got a Medication");
                org.hl7.fhir.dstu3.model.Medication medication = (org.hl7.fhir.dstu3.model.Medication) entry.getResource();
                LOG.info("Converting to R4");
                Medication newOne = convertMedicationToR4(ctx, medication);
                BundleEntryComponent newItem = new BundleEntryComponent().setResource(newOne);
                LOG.info("Adding to the list");
                newEntries.add(newItem);
                total++;
            }

        }
        resultsList.setEntry(newEntries);
        LOG.info("call() is returning a Bundle of " + resultsList.getEntry().size() + " resources");
        resultsList.setTotal(total);
        return resultsList;
    }

    /**
     * Function to actually call GP Connect
     *
     * @param ctx
     * @param NHSNumber
     */
    Bundle actualCall(FhirContext ctx, String NHSNumber) {
        LOG.info("In actualCall for patient: " + NHSNumber);
        Bundle response = new Bundle();
        try {
            ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
            String serverBase = "https://orange.testlab.nhs.uk/B82617/STU3/1/gpconnect/structured/fhir";
            IGenericClient client = ctx.newRestfulGenericClient(serverBase);
            IParser parser = ctx.newJsonParser();

            Identifier patientID = new Identifier();
            patientID.setSystem("https://fhir.nhs.uk/Id/nhs-number");
            patientID.setValue(NHSNumber);

            // Here we're adding the JWT:
            String token = createToken();
            LOG.info("Token created: " + token);
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
            client.registerInterceptor(authInterceptor);

            // Here we add Headers:
            AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
            interceptor.addHeaderValue("Ssp-TraceID", UUID.randomUUID().toString());
            interceptor.addHeaderValue("Ssp-From", "200000000359");
            interceptor.addHeaderValue("Ssp-To", "918999198738");
            interceptor.addHeaderValue("Ssp-InteractionID", "urn:nhs:names:services:gpconnect:fhir:operation:gpc.getstructuredrecord-1");
            client.registerInterceptor(interceptor);

            Parameters inParams = new Parameters();
            inParams.addParameter().setName("patientNHSNumber").setValue(patientID);
            inParams.addParameter().setName("includeMedication");
            LOG.info("Got params:" + parser.encodeResourceToString(inParams));

            LOG.info("About to call server...");
            Parameters outParams = client
                    .operation()
                    .onType(org.hl7.fhir.dstu3.model.Patient.class)
                    .named("$gpc.getstructuredrecord")
                    .withParameters(inParams)
                    .execute();
            LOG.info("Got response:" + parser.encodeResourceToString(outParams));

            List<Parameters.ParametersParameterComponent> outParameters = outParams.getParameter();
            LOG.info("Response has: " + outParameters.toArray().length);

            Parameters.ParametersParameterComponent returnedParameter = outParameters.get(0);
            response = (Bundle) returnedParameter.getResource();

            for (Parameters.ParametersParameterComponent parameter : outParameters) {
                LOG.info("Parameter: " + parameter.getName());
            }
        } catch (Exception ex) {
            LOG.warning("Exception caught: " + ex.getMessage() + " cause: " + ex.getCause());
        }
        return response;
    }

    public String createToken() {
        String token = "";
        Date now = new Date();
        Date issued = new Date(now.getTime());
        Date expires = new Date(now.getTime() + (1000 * 300));
        try {
            Algorithm algorithm = Algorithm.none();

            Gson gson = new Gson();

            String requesting_deviceJSON = "{\"resourceType\": \"Device\",\"identifier\":[{\"system\": \"https://orange.testlab.nhs.uk/gpconnect-demonstrator/Id/local-system-instance-id\",\"value\": \"gpcdemonstrator-1-orange\"}],\"model\": \"GP Connect Demonstrator\",\"version\": \"1.5.0\"}";
            Map requesting_device = gson.fromJson(requesting_deviceJSON, Map.class
            );

            String requesting_organizationJSON = "{\"resourceType\": \"Organization\",\"identifier\": [{\"system\": \"https://fhir.nhs.uk/Id/ods-organization-code\",\"value\": \"A11111\"}],\"name\": \"Consumer organisation name\"}";
            Map requesting_organization = gson.fromJson(requesting_organizationJSON, Map.class);

            String requesting_practitionerJSON = "{\"resourceType\": \"Practitioner\",\"id\": \"1\",\"identifier\": [{\"system\": \"https://fhir.nhs.uk/Id/sds-user-id\",\"value\": \"111111111111\"},{\"system\": \"https://fhir.nhs.uk/Id/sds-role-profile-id\",\"value\": \"22222222222222\"},{\"system\": \"https://orange.testlab.nhs.uk/gpconnect-demonstrator/Id/local-user-id\",\"value\": \"1\"}],\"name\": [{\"family\": \"Demonstrator\",\"given\": [\"GPConnect\"],\"prefix\": [\"Dr\"]}]}";
            Map requesting_practitioner = gson.fromJson(requesting_practitionerJSON, Map.class);

            token = JWT.create()
                    .withIssuer("https://orange.testlab.nhs.uk/")
                    .withSubject("1")
                    .withAudience("https://orange.testlab.nhs.uk/B82617/STU3/1/gpconnect/structured/fhir")
                    .withIssuedAt(issued)
                    .withExpiresAt(expires)
                    .withClaim("reason_for_request", "directcare")
                    .withClaim("requested_scope", "patient/*.read")
                    .withClaim("requesting_device", requesting_device)
                    .withClaim("requesting_organization", requesting_organization)
                    .withClaim("requesting_practitioner", requesting_practitioner)
                    .sign(algorithm);

            LOG.info(
                    "The token: " + token);
        } catch (JWTCreationException exception) {
            LOG.warning(exception.getMessage());
        }
        return token;
    }

    /**
     * Method to convert a MedicationRequest from STU3 to R4
     *
     * @param ctx The STU3 context
     * @param input The STU3 Resource
     * @return An R4 MedicationRequest
     */
    public MedicationRequest convertMedicationRequestToR4(FhirContext ctx, org.hl7.fhir.dstu3.model.MedicationRequest input) {
        IParser parser = ctx.newJsonParser();
        LOG.info("STU3 input:\n" + parser.encodeResourceToString(input));
        MedicationRequest output = null;
        output = (MedicationRequest) VersionConvertor_30_40.convertResource(input, true);
        return output;
    }

    /**
     * Method to convert a Medication from STU3 to R4
     *
     * @param ctx The STU3 context
     * @param input The STU3 Resource
     * @return An R4 Medication
     */
    public Medication convertMedicationToR4(FhirContext ctx, org.hl7.fhir.dstu3.model.Medication input) {
        IParser parser = ctx.newJsonParser();
        LOG.info("STU3 input:\n" + parser.encodeResourceToString(input));
        Medication output = null;
        output = (Medication) VersionConvertor_30_40.convertResource(input, true);
        return output;
    }
}
