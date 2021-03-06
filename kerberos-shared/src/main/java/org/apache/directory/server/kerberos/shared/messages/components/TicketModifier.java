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
package org.apache.directory.server.kerberos.shared.messages.components;


import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.directory.server.kerberos.shared.messages.value.EncryptedData;
import org.apache.directory.server.kerberos.shared.messages.value.KerberosPrincipalModifier;
import org.apache.directory.server.kerberos.shared.messages.value.PrincipalName;


public class TicketModifier
{
    private int ticketVersionNumber;
    private KerberosPrincipalModifier serverModifier = new KerberosPrincipalModifier();
    private EncryptedData encPart;


    public Ticket getTicket()
    {
        KerberosPrincipal serverPrincipal = serverModifier.getKerberosPrincipal();
        return new Ticket( ticketVersionNumber, serverPrincipal, encPart );
    }


    public void setTicketVersionNumber( int versionNumber )
    {
        ticketVersionNumber = versionNumber;
    }


    public void setEncPart( EncryptedData part )
    {
        encPart = part;
    }


    public void setServerName( PrincipalName name )
    {
        serverModifier.setPrincipalName( name );
    }


    public void setServerRealm( String realm )
    {
        serverModifier.setRealm( realm );
    }
}
