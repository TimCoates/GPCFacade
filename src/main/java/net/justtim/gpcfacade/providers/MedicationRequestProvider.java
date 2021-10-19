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
package net.justtim.gpcfacade.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.logging.Logger;
import net.justtim.gpcfacade.gpconnect.GetGPCRecord;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;

/**
 *
 * @author tim.coates@nhs.net
 */
public class MedicationRequestProvider implements IResourceProvider {

    FhirContext ctx;
    IParser parser;
    private static final Logger LOG = Logger.getLogger(MedicationRequestProvider.class.getName());

    public MedicationRequestProvider(FhirContext ctx) {
        this.ctx = ctx;
        parser = ctx.newJsonParser();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationRequest.class;
    }

    /**
     * This handles the search by NHS Number
     *
     * @param theId
     * @return
     */
    @Search()
    public Bundle searchByNHSNumber(
            @RequiredParam(name = MedicationRequest.SP_PATIENT) TokenParam theId) {
        String stringVer = theId.toString();
        LOG.info("stringVer: " + stringVer);
        String identifier = theId.getValue();
        LOG.info("Got identifier: " + identifier);

        String NHSNumber = identifier.substring(identifier.length() - 10);;
        LOG.info("Got NHS Number: " + NHSNumber);

        // So now we're going to call GP Connect Get Structured Record for this fella...
        GetGPCRecord caller = new GetGPCRecord();
        Bundle retVal = new Bundle();
        retVal.setType(Bundle.BundleType.SEARCHSET);

        LOG.info("Calling call() function with: " + NHSNumber);
        retVal = caller.call(NHSNumber, "MedicationRequest");
        LOG.info("searchByNHSNumber() is returning: " + retVal.getEntry().size() + " Resources");
        LOG.info("retVal: " + parser.encodeResourceToString(retVal));
        return retVal;
    }

}
