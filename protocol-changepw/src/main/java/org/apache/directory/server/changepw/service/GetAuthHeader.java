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
package org.apache.directory.server.changepw.service;


import org.apache.directory.server.changepw.messages.ChangePasswordRequest;
import org.apache.directory.server.kerberos.shared.messages.ApplicationRequest;
import org.apache.directory.server.kerberos.shared.messages.components.Ticket;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.chain.IoHandlerCommand;


/**
 * Differs from the TGS getAuthHeader by not verifying the presence of TGS_REQ.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class GetAuthHeader implements IoHandlerCommand
{
    private String contextKey = "context";

    public void execute( NextCommand next, IoSession session, Object message ) throws Exception
    {
        ChangePasswordContext changepwContext = ( ChangePasswordContext ) session.getAttribute( getContextKey() );
        ChangePasswordRequest request = ( ChangePasswordRequest ) changepwContext.getRequest();

        ApplicationRequest authHeader = request.getAuthHeader();
        Ticket ticket = authHeader.getTicket();

        changepwContext.setAuthHeader( authHeader );
        changepwContext.setTicket( ticket );

        next.execute( session, message );
    }


    public String getContextKey()
    {
        return ( this.contextKey );
    }
}
