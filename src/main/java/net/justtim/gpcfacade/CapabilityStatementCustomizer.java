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
package net.justtim.gpcfacade;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.DateTimeType;

/**
 *
 * @author tim.coates@nhs.net
 */
@Interceptor
public class CapabilityStatementCustomizer {

    @Hook(Pointcut.SERVER_CAPABILITY_STATEMENT_GENERATED)
    public void customize(IBaseConformance theCapabilityStatement) {

        // Cast to the appropriate version
        CapabilityStatement cs = (CapabilityStatement) theCapabilityStatement;

        // Customize the CapabilityStatement as desired
        cs
                .getSoftware()
                .setName("GPCFacade")
                .setVersion("0.1")
                .setReleaseDateElement(new DateTimeType("2021-10-20"));
        cs.setCopyright("Copyright (C) 2021 NHS Digital.");
        ContactDetail details = new ContactDetail().setName("");
        List<ContactDetail> detailList = new ArrayList<>();
        detailList.add(details);
        cs.setContact(detailList);

    }

}
