/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.qunit.api.pages;

import org.jboss.arquillian.qunit.api.model.QUnitTest;

/**
 * An abstraction to describe the QUnit Test Page.
 * 
 * @author Lukas Fryc
 * @author Tolis Emmanouilidis
 * 
 */
public interface QUnitTestPage {

    /**
     * Waits until the tests execution is completed.
     */
    void waitUntilTestsExecutionIsCompleted();

    /**
     * Finds how many tests exist inside the QUnit page.
     * 
     * @return Tests size
     */
    int getTestsSize();

    /**
     * Returns the QUnit tests.
     * 
     * @return {@link QUnitTest}
     */
    QUnitTest[] getTests();
}
