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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.openapi.OpenApiInterceptor;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import net.justtim.gpcfacade.providers.MedicationRequestProvider;
import net.justtim.gpcfacade.providers.MedicationStatementProvider;

/**
 *
 * @author tim.coates@nhs.net
 */
@WebServlet("/*")
public class MyFHIRServer extends RestfulServer {

    @Override
    protected void initialize() throws ServletException {

        FhirContext ctx = FhirContext.forR4();
        // Create a context for the appropriate version
        setFhirContext(ctx);

        // Register resource providers
        List<IResourceProvider> providers = new ArrayList<>();
        providers.add(new MedicationStatementProvider(ctx));
        providers.add(new MedicationRequestProvider(ctx));
        setResourceProviders(providers);

        registerInterceptor(new OpenApiInterceptor());
    }
}
