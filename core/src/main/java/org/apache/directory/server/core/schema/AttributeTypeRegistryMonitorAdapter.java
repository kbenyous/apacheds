/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.core.schema;


import org.apache.directory.shared.ldap.schema.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple do nothing monitor adapter for AttributeTypeRegistries.  Note for
 * safty exception based callback print the stack tract to stderr.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class AttributeTypeRegistryMonitorAdapter implements AttributeTypeRegistryMonitor
{
    private static final Logger log = LoggerFactory.getLogger( AttributeTypeRegistryMonitorAdapter.class );


    public void registered( AttributeType attributeType )
    {
    }


    public void lookedUp( AttributeType attributeType )
    {
    }


    public void lookupFailed( String oid, Throwable fault )
    {
        if ( fault != null )
        {
            log.warn( "Failed to look up the attribute type registry:" + oid, fault );
        }
    }


    public void registerFailed( AttributeType attributeType, Throwable fault )
    {
        if ( fault != null )
        {
            log.warn( "Failed to register an attribute type to the attribute type registry: " + attributeType, fault );
        }
    }
}
