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
package org.apache.directory.server.kerberos.kdc;


import org.apache.directory.server.kerberos.shared.crypto.encryption.EncryptionType;
import org.apache.directory.server.kerberos.shared.messages.KdcRequest;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.chain.IoHandlerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonitorRequest implements IoHandlerCommand
{
    /** the log for this class */
    private static final Logger log = LoggerFactory.getLogger( MonitorRequest.class );

    private String contextKey = "context";

    public void execute( NextCommand next, IoSession session, Object message ) throws Exception
    {
        KdcContext kdcContext = ( KdcContext ) session.getAttribute( getContextKey() );
        KdcRequest request = kdcContext.getRequest();
        String clientAddress = kdcContext.getClientAddress().getHostAddress();

        if ( log.isDebugEnabled() )
        {
            log.debug( "Responding to authentication request:" + "\n\trealm:                 " + request.getRealm()
                + "\n\tserverPrincipal:       " + request.getServerPrincipal() + "\n\tclientPrincipal:       "
                + request.getClientPrincipal() + "\n\tclientAddress:         " + clientAddress
                + "\n\thostAddresses:         " + request.getAddresses() + "\n\tencryptionType:        "
                + getEncryptionTypes( request ) + "\n\tfrom krb time:         " + request.getFrom()
                + "\n\trealm krb time:        " + request.getRtime() + "\n\tkdcOptions:            "
                + request.getKdcOptions() + "\n\tmessageType:           " + request.getMessageType()
                + "\n\tnonce:                 " + request.getNonce() + "\n\tprotocolVersionNumber: "
                + request.getProtocolVersionNumber() + "\n\ttill:                  " + request.getTill() );
        }

        next.execute( session, message );
    }


    public String getEncryptionTypes( KdcRequest request )
    {
        EncryptionType[] etypes = request.getEType();

        StringBuffer sb = new StringBuffer();

        for ( int ii = 0; ii < etypes.length; ii++ )
        {
            sb.append( etypes[ii].toString() );

            if ( ii < etypes.length - 1 )
            {
                sb.append( ", " );
            }
        }

        return sb.toString();
    }
    
    public String getContextKey()
    {
        return ( this.contextKey );
    }
}
