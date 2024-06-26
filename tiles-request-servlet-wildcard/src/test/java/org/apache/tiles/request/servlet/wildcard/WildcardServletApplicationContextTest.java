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
package org.apache.tiles.request.servlet.wildcard;

import junit.framework.TestCase;

import org.apache.tiles.request.locale.URLApplicationResource;
import org.easymock.EasyMock;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import java.util.HashSet;

import jakarta.servlet.ServletContext;


/**
 * Tests {@link WildcardServletApplicationContext}.
 *
 * @version $Rev$ $Date$
 */
public class WildcardServletApplicationContextTest extends TestCase {

    /**
     * Number of properties container inside the test.properties file.
     */
    private static final int TEST_PROPERTIES_SIZE = 3;

    /**
     * The root Tiles application context.
     */
    private ServletContext servletContext;

    /**
     * The enhanced Tiles application context.
     */
    private WildcardServletApplicationContext context;

    /**
     * The original class loader.
     */
    private ClassLoader original;

    /** {@inheritDoc} */
    @Override
    public void setUp() {
        servletContext = EasyMock.createMock(ServletContext.class);
        original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(new MockClassLoader());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error when using the mock classloader");
        }
        context = new WildcardServletApplicationContext(servletContext);
    }

    /** {@inheritDoc} */
    @Override
    protected void tearDown() {
        Thread.currentThread().setContextClassLoader(original);
    }

    /**
     * Tests resource getting.
     *
     * @throws IOException If something goes wrong.
     */
    public void testGetResources() throws IOException {
        String url = "/test.properties";
        HashSet<URL> set = new HashSet<URL>();
        URL u = new URL("file://tiles/test.properties");
        set.add(u);
        EasyMock.expect(servletContext.getResource(url)).andReturn(u)
                .anyTimes();
        File dir = new File(".");
        EasyMock.expect(servletContext.getResource("/WEB-INF/")).andReturn(
                dir.toURI().toURL()).anyTimes();
        URL pomUrl = new URL("file://tiles/pom.xml");
        EasyMock.expect(servletContext.getResource("/WEB-INF/pom.xml"))
                .andReturn(pomUrl).anyTimes();
        Set<String> elementSet = new HashSet<String>();
        elementSet.add("/WEB-INF/pom.xml");
        EasyMock.expect(servletContext.getResourcePaths("/WEB-INF/")).andReturn(elementSet).anyTimes();
        EasyMock.replay(servletContext);

        assertEquals(new URLApplicationResource(u.toExternalForm(), u), context.getResource(url));
        assertEquals(new URLApplicationResource(pomUrl.toExternalForm(), pomUrl), context.getResource("/WEB-INF/*.xml"));
        assertEquals(TEST_PROPERTIES_SIZE, context.getResources(
                "classpath*:/test.properties").size());

        assertEquals(1, context.getResources(
                "classpath*:/org/apache/tiles/request/servlet/wildcard/*Test.class").size());
        EasyMock.verify(servletContext);
    }

    /**
     * An mock class loader.
     */
    public class MockClassLoader extends ClassLoader {

        /**
         * A vector of resources.
         */
        private Vector<URL> testPropertiesResources;

        /**
         * Constructor.
         *
         * @throws MalformedURLException If the URL is not valid (that should
         * not happen).
         */
        public MockClassLoader() throws MalformedURLException {
            testPropertiesResources = new Vector<URL>();
            testPropertiesResources.add(new URL("file://tiles/test/test.properties"));
            testPropertiesResources.add(new URL("file://tiles/two/test.properties"));
            testPropertiesResources.add(new URL("file://tiles/three/test.properties"));
        }

        /** {@inheritDoc} */
        @Override
        public Enumeration<URL> findResources(String path) throws IOException {
            Enumeration<URL> retValue = null;
            if ("test.properties".equals(path)) {
                retValue = testPropertiesResources.elements();
            } else {
                retValue = super.findResources(path);
            }

            return retValue;
        }
    }
}
