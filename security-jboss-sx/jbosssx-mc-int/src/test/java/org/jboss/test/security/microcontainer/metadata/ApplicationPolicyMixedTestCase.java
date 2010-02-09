/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.security.microcontainer.metadata;

import org.jboss.security.microcontainer.beans.ApplicationPolicyBean;
import org.jboss.security.microcontainer.beans.AuthenticationPolicyBean;

/**
 * <p>
 * Extends the {@code BasicApplicationPolicyTestCase} to test the configuration of application policies mixed with
 * regular beans.
 * </p>
 * <p>
 * The deployed configuration file declares the basic application policies and a couple of beans:
 * 
 * <pre>
 *    &lt;!-- regular microcontainer beans --&gt;
 *  &lt;bean name=&quot;RegularBean$AuthenticationPolicy&quot; class=&quot;org.jboss.security.microcontainer.beans.AuthenticationPolicyBean&quot;/&gt;
 * 
 *  &lt;bean name=&quot;RegularBean&quot; class=&quot;org.jboss.security.microcontainer.beans.ApplicationPolicyBean&quot;&gt;
 *     &lt;property name=&quot;name&quot;&gt;RegularBean&lt;/property&gt;
 *     &lt;property name=&quot;authenticationPolicy&quot;&gt;&lt;inject bean=&quot;RegularBean$AuthenticationPolicy&quot;/&gt;&lt;/property&gt;
 *  &lt;/bean&gt;
 * 
 *  &lt;!-- a security application policy that specifies an authentication configuration --&gt;
 *  &lt;application-policy xmlns=&quot;urn:jboss:security-beans:1.0&quot; name=&quot;TestPolicy1&quot;&gt;
 *     &lt;authentication&gt;
 *        &lt;login-module code=&quot;org.jboss.security.auth.AuthModule1&quot; flag=&quot;required&quot;&gt;
 *           &lt;module-option name=&quot;authOption1&quot;&gt;value1&lt;/module-option&gt;
 *           &lt;module-option name=&quot;authOption2&quot;&gt;value2&lt;/module-option&gt;
 *        &lt;/login-module&gt;
 *        &lt;login-module code=&quot;org.jboss.security.auth.AuthModule2&quot; flag=&quot;optional&quot;&gt;
 *           &lt;module-option name=&quot;authOption3&quot;&gt;value3&lt;/module-option&gt;
 *           &lt;module-option name=&quot;authOption4&quot;&gt;value4&lt;/module-option&gt;
 *        &lt;/login-module&gt;
 *     &lt;/authentication&gt;
 *  &lt;/application-policy&gt;
 * 
 *  &lt;!-- a security application policy that specifies an authentication-jaspi configuration --&gt;
 *  &lt;application-policy xmlns=&quot;urn:jboss:security-beans:1.0&quot; name=&quot;TestPolicy2&quot;&gt;
 *     &lt;authentication-jaspi&gt;
 *        &lt;login-module-stack name=&quot;ModuleStack1&quot;&gt;
 *           &lt;login-module code=&quot;org.jboss.security.auth.AuthModule3&quot; flag=&quot;required&quot;&gt;
 *              &lt;module-option name=&quot;authOption5&quot;&gt;value5&lt;/module-option&gt;
 *           &lt;/login-module&gt;
 *           &lt;login-module code=&quot;org.jboss.security.auth.AuthModule4&quot; flag=&quot;optional&quot;/&gt;
 *        &lt;/login-module-stack&gt;
 *        &lt;login-module-stack name=&quot;ModuleStack2&quot;&gt;
 *           &lt;login-module code=&quot;org.jboss.security.auth.AuthModule5&quot; flag=&quot;required&quot;&gt;
 *              &lt;module-option name=&quot;authOption6&quot;&gt;value6&lt;/module-option&gt;
 *              &lt;module-option name=&quot;authOption7&quot;&gt;value7&lt;/module-option&gt;
 *           &lt;/login-module&gt;
 *        &lt;/login-module-stack&gt;
 *        &lt;auth-module code=&quot;org.jboss.security.auth.AuthModule1&quot; login-module-stack-ref=&quot;ModuleStack2&quot;&gt;
 *           &lt;module-option name=&quot;authOption1&quot;&gt;value1&lt;/module-option&gt;
 *           &lt;module-option name=&quot;authOption2&quot;&gt;value2&lt;/module-option&gt;
 *        &lt;/auth-module&gt;
 *     &lt;/authentication-jaspi&gt;
 *  &lt;/application-policy&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class ApplicationPolicyMixedTestCase extends BasicApplicationPolicyTestCase
{

   /**
    * <p>
    * Creates an instance of {@code ApplicationPolicyMixedTestCase} with the specified name.
    * </p>
    * 
    * @param name a {@code String} containing the name of this test case.
    */
   public ApplicationPolicyMixedTestCase(String name)
   {
      super(name);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.test.security.microcontainer.beans.BasicApplicationPolicyTestCase#testApplicationPoliciesCreation()
    */
   @Override
   public void testApplicationPoliciesCreation() throws Exception
   {
      // validate the basic application policy creation.
      super.testApplicationPoliciesCreation();

      // check the regular beans have been instantiated.
      AuthenticationPolicyBean authBean = (AuthenticationPolicyBean) super.getBean("RegularBean$AuthenticationPolicy");
      assertNotNull("Authentication policy bean not found", authBean);

      ApplicationPolicyBean appPolicyBean = (ApplicationPolicyBean) super.getBean("RegularBean");
      assertNotNull("Application policy bean not found", appPolicyBean);
      assertEquals("Incorrect policy name", "RegularBean", appPolicyBean.getName());
      assertEquals("Unexpected authentication policy bean found", authBean, appPolicyBean.getAuthenticationPolicy());
   }
}