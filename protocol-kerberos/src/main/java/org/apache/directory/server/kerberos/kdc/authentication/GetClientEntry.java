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
package org.apache.directory.server.kerberos.kdc.authentication;


import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.directory.server.kerberos.shared.exceptions.ErrorType;
import org.apache.directory.server.kerberos.shared.service.GetPrincipalStoreEntry;
import org.apache.directory.server.kerberos.shared.store.PrincipalStore;
import org.apache.mina.common.IoSession;


public class GetClientEntry extends GetPrincipalStoreEntry
{
    public void execute( NextCommand next, IoSession session, Object message ) throws Exception
    {
        AuthenticationContext authContext = ( AuthenticationContext ) session.getAttribute( getContextKey() );

        KerberosPrincipal principal = authContext.getRequest().getClientPrincipal();
        PrincipalStore store = authContext.getStore();

        authContext.setClientEntry( getEntry( principal, store, ErrorType.KDC_ERR_C_PRINCIPAL_UNKNOWN ) );

        next.execute( session, message );
    }
}
