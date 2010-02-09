/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.security.auth.message.config;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.AuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;

import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.auth.callback.JBossCallbackHandler;
import org.jboss.security.auth.container.config.AuthModuleEntry;
import org.jboss.security.auth.container.modules.DelegatingServerAuthModule;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.auth.login.BaseAuthenticationInfo;
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.security.config.ControlFlag;
import org.jboss.security.config.SecurityConfiguration;

//$Id$

/**
 *  Provides configuration for the server side
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  May 15, 2006 
 *  @version $Revision$
 */
public class JBossServerAuthConfig implements ServerAuthConfig
{
   private String layer;
   private String contextId;
   private CallbackHandler callbackHandler = new JBossCallbackHandler();
   @SuppressWarnings("unchecked")
   private List modules = new ArrayList();
   @SuppressWarnings({"unused", "unchecked"})
   private Map contextProperties;

   /**
    * Create a new JBossServerAuthConfig.
    * 
    * @param layer Message Layer
    * @param appContext Application Context
    * @param handler Callback Handler that will be passed to the modules
    * @param properties Context Properties
    */
   @SuppressWarnings("unchecked")
   public JBossServerAuthConfig(String layer, String appContext,
         CallbackHandler handler, Map properties)
   {
      this.layer = layer;
      this.contextId = appContext;
      this.callbackHandler = handler;
      this.contextProperties = properties;
   }
   
   /**
    * @see ServerAuthConfig#getAuthContext(String, Map)
    */
   @SuppressWarnings("unchecked")
   public ServerAuthContext getAuthContext(String authContextID,
         Subject serviceSubject, Map properties) 
   throws AuthException
   { 
      List<ControlFlag> controlFlags = new ArrayList<ControlFlag>();
      
      Map<String,Map> mapOptionsByName = new HashMap<String,Map>();
      SecurityContext securityContext = SecurityActions.getSecurityContext();
      if(securityContext == null)
         throw new IllegalStateException("Security Context is null");
      String secDomain = securityContext.getSecurityDomain();
      
      String defaultAppDomain = SecurityConstants.DEFAULT_APPLICATION_POLICY;
      //Get the modules from the SecurityConfiguration
      ApplicationPolicy ap = SecurityConfiguration.getApplicationPolicy(secDomain);
      if(ap == null)
      {
         ap = SecurityConfiguration.getApplicationPolicy(defaultAppDomain);
      }
      if(ap == null)
         throw new IllegalStateException("No Application Policy found");
      BaseAuthenticationInfo bai = ap.getAuthenticationInfo();
      if(bai == null)
         throw new IllegalStateException("Authentication Info not set in security domain="+ secDomain 
               + " or "+ defaultAppDomain);
      
      if(bai instanceof AuthenticationInfo)
      {
         //Need to get a wrapper
         ServerAuthModule sam = new DelegatingServerAuthModule();
         Map options = new HashMap();
         options.put("javax.security.auth.login.LoginContext", secDomain); //Name of sec domain
         sam.initialize(null, null, this.callbackHandler, options); 
         modules.add(sam);
      }
      else
      {
         JASPIAuthenticationInfo jai = (JASPIAuthenticationInfo)bai;
         AuthModuleEntry[] amearr = jai.getAuthModuleEntry();
         for(AuthModuleEntry ame: amearr)
         {
            if(ame.getLoginModuleStackHolderName() != null)
            {
               try
               {
                  mapOptionsByName.put(ame.getAuthModuleName(), ame.getOptions());
                  controlFlags.add(ame.getControlFlag());   
                  ServerAuthModule sam = this.createSAM(ame.getAuthModuleName(), 
                        ame.getLoginModuleStackHolderName());
                  
                  Map options = new HashMap();
                  
                  sam.initialize(null, null, callbackHandler, options);
                  modules.add(sam);
               }
               catch (Exception e)
               {
                  throw new AuthException(e.getLocalizedMessage());
               }
            }
            else
            {
               try
               {
                  mapOptionsByName.put(ame.getAuthModuleName(), ame.getOptions());
                  controlFlags.add(ame.getControlFlag());             
                  ServerAuthModule sam = this.createSAM(ame.getAuthModuleName());
                  
                  Map options = new HashMap(); 
                  sam.initialize(null, null, callbackHandler, options);
                  modules.add(sam);
               }
               catch (Exception e)
               {
                  throw new AuthException(e.getLocalizedMessage());
               }
            }
         } 
      } 
       
      JBossServerAuthContext serverAuthContext = new JBossServerAuthContext(modules, mapOptionsByName, this.callbackHandler);
      serverAuthContext.setControlFlags(controlFlags);
      return serverAuthContext;
   }
 
   /**
    * @see AuthConfig#getContextID()
    */
   public String getAppContext()
   { 
      return this.contextId;
   } 
   
   /**
    * @see AuthConfig#getMessageLayer()
    */
   public String getMessageLayer()
   {
      return this.layer;
   }
 

   /**
    * @see AuthConfig#refresh()
    */
   public void refresh() throws AuthException, SecurityException
   { 
   } 
   
   //Custom Methods
   @SuppressWarnings("unchecked")
   public List getServerAuthModules()
   {
      return this.modules ;
   }

   public String getAuthContextID(MessageInfo messageInfo)
   {
      return this.contextId;
   }

   public boolean isProtected()
   {
      throw new RuntimeException("Not Implemented");
   }  

   @SuppressWarnings("unchecked")
   private ServerAuthModule createSAM(String name )
   throws Exception
   {
      Class clazz = SecurityActions.getContextClassLoader().loadClass(name);
      Constructor ctr = clazz.getConstructor(new Class[0]);
      return (ServerAuthModule) ctr.newInstance(new Object[0]);
   }
   
   @SuppressWarnings("unchecked")
   private ServerAuthModule createSAM(String name, String lmshName )
   throws Exception
   {
      Class clazz = SecurityActions.getContextClassLoader().loadClass(name);
      Constructor ctr = clazz.getConstructor(new Class[]{String.class});
      return (ServerAuthModule) ctr.newInstance(new Object[]{lmshName});
   }
}