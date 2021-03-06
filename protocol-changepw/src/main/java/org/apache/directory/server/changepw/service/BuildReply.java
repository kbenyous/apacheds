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


import java.net.InetAddress;

import org.apache.directory.server.changepw.exceptions.ChangePasswordException;
import org.apache.directory.server.changepw.exceptions.ErrorType;
import org.apache.directory.server.changepw.messages.ChangePasswordReplyModifier;
import org.apache.directory.server.kerberos.shared.exceptions.KerberosException;
import org.apache.directory.server.kerberos.shared.messages.application.ApplicationReply;
import org.apache.directory.server.kerberos.shared.messages.application.PrivateMessage;
import org.apache.directory.server.kerberos.shared.messages.components.Authenticator;
import org.apache.directory.server.kerberos.shared.messages.components.EncApRepPart;
import org.apache.directory.server.kerberos.shared.messages.components.EncApRepPartModifier;
import org.apache.directory.server.kerberos.shared.messages.components.EncKrbPrivPart;
import org.apache.directory.server.kerberos.shared.messages.components.EncKrbPrivPartModifier;
import org.apache.directory.server.kerberos.shared.messages.components.Ticket;
import org.apache.directory.server.kerberos.shared.messages.value.EncryptedData;
import org.apache.directory.server.kerberos.shared.messages.value.EncryptionKey;
import org.apache.directory.server.kerberos.shared.messages.value.HostAddress;
import org.apache.directory.server.kerberos.shared.service.LockBox;
import org.apache.mina.common.IoSession;
import org.apache.mina.handler.chain.IoHandlerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class BuildReply implements IoHandlerCommand
{
    /** the log for this class */
    private static final Logger log = LoggerFactory.getLogger( BuildReply.class );

    private String contextKey = "context";

    public void execute( NextCommand next, IoSession session, Object message ) throws Exception
    {
        ChangePasswordContext changepwContext = ( ChangePasswordContext ) session.getAttribute( getContextKey() );

        Authenticator authenticator = changepwContext.getAuthenticator();
        Ticket ticket = changepwContext.getTicket();
        LockBox lockBox = changepwContext.getLockBox();

        // begin building reply

        // create priv message
        // user-data component is short result code
        EncKrbPrivPartModifier modifier = new EncKrbPrivPartModifier();
        byte[] resultCode =
            { ( byte ) 0x00, ( byte ) 0x00 };
        modifier.setUserData( resultCode );

        modifier.setSenderAddress( new HostAddress( InetAddress.getLocalHost() ) );
        EncKrbPrivPart privPart = modifier.getEncKrbPrivPart();

        // get the subsession key from the Authenticator
        EncryptionKey subSessionKey = authenticator.getSubSessionKey();

        EncryptedData encPrivPart;

        try
        {
            encPrivPart = lockBox.seal( subSessionKey, privPart );
        }
        catch ( KerberosException ke )
        {
            log.error( ke.getMessage(), ke );
            throw new ChangePasswordException( ErrorType.KRB5_KPASSWD_SOFTERROR );
        }

        PrivateMessage privateMessage = new PrivateMessage( encPrivPart );

        // Begin AP_REP generation
        EncApRepPartModifier encApModifier = new EncApRepPartModifier();
        encApModifier.setClientTime( authenticator.getClientTime() );
        encApModifier.setClientMicroSecond( authenticator.getClientMicroSecond() );
        encApModifier.setSequenceNumber( new Integer( authenticator.getSequenceNumber() ) );
        encApModifier.setSubSessionKey( authenticator.getSubSessionKey() );

        EncApRepPart repPart = encApModifier.getEncApRepPart();

        EncryptedData encRepPart;

        try
        {
            encRepPart = lockBox.seal( ticket.getSessionKey(), repPart );
        }
        catch ( KerberosException ke )
        {
            log.error( ke.getMessage(), ke );
            throw new ChangePasswordException( ErrorType.KRB5_KPASSWD_SOFTERROR );
        }

        ApplicationReply appReply = new ApplicationReply( encRepPart );

        // return status message value object
        ChangePasswordReplyModifier replyModifier = new ChangePasswordReplyModifier();
        replyModifier.setApplicationReply( appReply );
        replyModifier.setPrivateMessage( privateMessage );

        changepwContext.setReply( replyModifier.getChangePasswordReply() );

        next.execute( session, message );
    }


    public String getContextKey()
    {
        return ( this.contextKey );
    }
}
