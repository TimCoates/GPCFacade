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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 *
 * @author tim.coates@nhs.net
 */
public class FHIRMainServer {

    public static void main(String[] args) throws Exception {
// Create a basic jetty server object that will listen on port 8080.

        Server server = createServer(8080);

        // Start things up!
        server.start();

        // The use of server.join() the will make the current thread join and
        // wait until the server thread is done executing.
        server.join();
    }

    public static Server createServer(int port) {
        // See: https://xy2401.com/local-docs/java/jetty.9.4.24.v20191120/embedded-examples.html#embedded-minimal-servlet

        // Note that if you set this to port 0 then a randomly available port
        // will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        Server server = new Server(port);

        // The ServletHandler is a dead simple way to create a context handler
        // that is backed by an instance of a Servlet.
        // This handler then needs to be registered with the Server object.
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        // Passing in the class for the Servlet allows jetty to instantiate an
        // instance of that Servlet and mount it on a given context path.
        // IMPORTANT:
        // This is a raw Servlet, not a Servlet that has been configured
        // through a web.xml @WebServlet annotation, or anything similar.
        //handler.addServletWithMapping(MyFHIRServer.class, "/*");
        return server;
    }

}
