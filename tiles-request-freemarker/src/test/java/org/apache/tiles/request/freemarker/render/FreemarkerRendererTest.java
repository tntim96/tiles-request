/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tiles.request.freemarker.render;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.render.CannotRenderException;
import org.apache.tiles.request.servlet.ServletApplicationContext;
import org.apache.tiles.request.servlet.ServletRequest;
import org.junit.Before;
import org.junit.Test;

import freemarker.ext.jakarta.servlet.HttpRequestHashModel;
import freemarker.ext.jakarta.servlet.HttpRequestParametersHashModel;
import freemarker.ext.jakarta.servlet.ServletContextHashModel;
import freemarker.template.ObjectWrapper;

/**
 * Tests {@link FreemarkerRenderer}.
 *
 * @version $Rev$ $Date$
 */
public class FreemarkerRendererTest {

    /**
     * The attribute name of the application model.
     */
    private static final String ATTR_APPLICATION_MODEL =
        ".freemarker.Application";

    /**
     * The attribute name of the JSP taglibs model.
     */
    private static final String ATTR_JSP_TAGLIBS_MODEL =
        ".freemarker.JspTaglibs";

    /**
     * The attribute name of the request model.
     */
    private static final String ATTR_REQUEST_MODEL = ".freemarker.Request";

    /**
     * The attribute name of the request parameters model.
     */
    private static final String ATTR_REQUEST_PARAMETERS_MODEL =
        ".freemarker.RequestParameters";

    /**
     * The renderer to test.
     */
    private FreemarkerRenderer renderer;

    /**
     * The application context.
     */
    private ApplicationContext applicationContext;

    /**
     * The servlet context.
     */
    private ServletContext servletContext;

    /**
     * Sets up the test.
     */
    @Before
    public void setUp() {
        applicationContext = createMock(ServletApplicationContext.class);
        servletContext = createMock(ServletContext.class);

        expect(applicationContext.getContext()).andReturn(servletContext);

        replay(applicationContext, servletContext);
        renderer = FreemarkerRendererBuilder.createInstance()
                .setApplicationContext(applicationContext)
                .setParameter("TemplatePath", "/")
                .setParameter("NoCache", "true")
                .setParameter("ContentType", "text/html")
                .setParameter("template_update_delay", "0")
                .setParameter("default_encoding", "ISO-8859-1")
                .setParameter("number_format", "0.##########").build();
    }

    /**
     * Tests {@link FreemarkerRenderer#render(String, org.apache.tiles.request.Request)}.
     * @throws IOException If something goes wrong.
     * @throws ServletException If something goes wrong.
     */
    @Test
    public void testWrite() throws IOException, ServletException {
        ApplicationContext applicationContext = createMock(ServletApplicationContext.class);
        ServletContext servletContext = createMock(ServletContext.class);
        GenericServlet servlet = createMockBuilder(GenericServlet.class).createMock();
        ServletConfig servletConfig = createMock(ServletConfig.class);
        ObjectWrapper objectWrapper = createMock(ObjectWrapper.class);

        expect(servletConfig.getServletContext()).andReturn(servletContext);

        replay(servlet, servletConfig);
        servlet.init(servletConfig);
        ServletContextHashModel servletContextHashModel = new ServletContextHashModel(servlet, objectWrapper);

        expect(applicationContext.getContext()).andReturn(servletContext).anyTimes();
        expect(servletContext.getRealPath(isA(String.class))).andReturn(null).anyTimes();
        URL resource = getClass().getResource("/test.ftl");
        expect(servletContext.getResource(isA(String.class))).andReturn(resource).anyTimes();
        expect(servletContext.getAttribute(ATTR_APPLICATION_MODEL)).andReturn(servletContextHashModel);
        expect(servletContext.getAttribute(ATTR_JSP_TAGLIBS_MODEL)).andReturn(null);

        replay(applicationContext, servletContext, objectWrapper);

        FreemarkerRenderer renderer = FreemarkerRendererBuilder
                .createInstance().setApplicationContext(applicationContext)
                .setParameter("TemplatePath", "/")
                .setParameter("NoCache", "true")
                .setParameter("ContentType", "text/html")
                .setParameter("template_update_delay", "0")
                .setParameter("default_encoding", "ISO-8859-1")
                .setParameter("number_format", "0.##########").build();

        ServletRequest request = createMock(ServletRequest.class);
        HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        HttpRequestHashModel requestModel = new HttpRequestHashModel(httpRequest, response, objectWrapper);
        HttpRequestParametersHashModel parametersModel = new HttpRequestParametersHashModel(httpRequest);

        expect(request.getRequest()).andReturn(httpRequest);
        expect(request.getResponse()).andReturn(response);
        expect(request.getPrintWriter()).andReturn(printWriter);
        expect(httpRequest.getSession(false)).andReturn(null);
        expect(httpRequest.getAttribute(ATTR_REQUEST_MODEL)).andReturn(requestModel);
        expect(httpRequest.getAttribute(ATTR_REQUEST_PARAMETERS_MODEL)).andReturn(parametersModel);
        response.setContentType("text/html; charset=ISO-8859-1");
        response.setHeader(eq("Cache-Control"), isA(String.class));
        response.setHeader("Pragma", "no-cache");
        response.setHeader(eq("Expires"), isA(String.class));

        replay(request, httpRequest, response);
        renderer.render("hello", request);
        stringWriter.close();
        assertTrue(stringWriter.toString().startsWith("Hello!"));
        verify(applicationContext, servletContext, request, httpRequest,
                response, servlet, servletConfig, objectWrapper);
    }

    /**
     * Tests {@link FreemarkerRenderer#render(String, org.apache.tiles.request.Request)}.
     * @throws IOException If something goes wrong.
     * @throws ServletException If something goes wrong.
     */
    @Test(expected = CannotRenderException.class)
    public void testRenderException1() throws IOException, ServletException {
        ApplicationContext applicationContext = createMock(ServletApplicationContext.class);
        ServletContext servletContext = createMock(ServletContext.class);
        GenericServlet servlet = createMockBuilder(GenericServlet.class).createMock();
        ServletConfig servletConfig = createMock(ServletConfig.class);
        ObjectWrapper objectWrapper = createMock(ObjectWrapper.class);

        replay(servlet, servletConfig);
        servlet.init(servletConfig);

        expect(applicationContext.getContext()).andReturn(servletContext).anyTimes();
        expect(servletContext.getRealPath(isA(String.class))).andReturn(null).anyTimes();
        URL resource = getClass().getResource("/test.ftl");
        expect(servletContext.getResource(isA(String.class))).andReturn(resource).anyTimes();

        replay(applicationContext, servletContext, objectWrapper);

        FreemarkerRenderer renderer = FreemarkerRendererBuilder
                .createInstance().setApplicationContext(applicationContext)
                .setParameter("TemplatePath", "/")
                .setParameter("NoCache", "true")
                .setParameter("ContentType", "text/html")
                .setParameter("template_update_delay", "0")
                .setParameter("default_encoding", "ISO-8859-1")
                .setParameter("number_format", "0.##########").build();

        ServletRequest request = createMock(ServletRequest.class);

        replay(request);
        try {
            renderer.render(null, request);
        } finally {
            verify(applicationContext, servletContext, request, servlet,
                    servletConfig, objectWrapper);
        }
    }

    /**
     * Test method for
     * {@link FreemarkerRenderer
     * #isRenderable(Object, org.apache.tiles.Attribute, org.apache.tiles.context.TilesRequestContext)}
     * .
     */
    @Test
    public void testIsRenderable() {
        assertTrue(renderer.isRenderable("/my/template.ftl", null));
        assertFalse(renderer.isRenderable("my/template.ftl", null));
        assertFalse(renderer.isRenderable("/my/template.jsp", null));
        verify(applicationContext, servletContext);
    }

}
