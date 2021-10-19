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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.justtim.gpcfacade.gpconnect.GetGPCRecord;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;

/**
 *
 * @author tim.coates@nhs.net
 */
public class EncounterProvider implements IResourceProvider {

    FhirContext ctx;
    IParser parser;
    private static final Logger LOG = Logger.getLogger(EncounterProvider.class.getName());

    public EncounterProvider(FhirContext ctx) {
        this.ctx = ctx;
        parser = ctx.newJsonParser();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Encounter.class;
    }

    /**
     * This handles the search by NHS Number
     *
     * @param theId
     * @return
     */
    @Search()
    public List<Encounter> searchByNHSNumber(
            @RequiredParam(name = Encounter.SP_PATIENT) TokenParam theId) {
        String identifier = theId.getValue();
        LOG.info("Got value: " + identifier);

        // So now we're going to call GP Connect Get Structured Record for this fella...
        GetGPCRecord caller = new GetGPCRecord();
        caller.call(identifier.split("/")[identifier.split("/").length - 1]);

        List<Encounter> retVal = new ArrayList<>();
        // ...populate...
        return retVal;
    }

}
