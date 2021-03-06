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


import java.nio.ByteBuffer;

import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.directory.server.changepw.ChangePasswordConfiguration;
import org.apache.directory.server.changepw.exceptions.ChangePasswordException;
import org.apache.directory.server.changepw.messages.ChangePasswordErrorModifier;
import org.apache.directory.server.kerberos.shared.exceptions.KerberosException;
import org.apache.directory.server.kerberos.shared.messages.ErrorMessage;
import org.apache.directory.server.kerberos.shared.messages.ErrorMessageModifier;
import org.apache.directory.server.kerberos.shared.messages.value.KerberosTime;
import org.apache.directory.server.protocol.shared.chain.Command;
import org.apache.directory.server.protocol.shared.chain.Context;
import org.apache.directory.server.protocol.shared.chain.Filter;
import org.apache.directory.server.protocol.shared.chain.impl.CommandBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link Command} for helping convert a {@link ChangePasswordException} into
 * an {@link ErrorMessage} to be returned to clients.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class ChangePasswordExceptionHandler extends CommandBase implements Filter
{
    private static final Logger log = LoggerFactory.getLogger( ChangePasswordExceptionHandler.class );


    public boolean execute( Context context ) throws Exception
    {
        return CONTINUE_CHAIN;
    }


    public boolean postprocess( Context context, Exception exception )
    {
        if ( exception == null )
        {
            return CONTINUE_CHAIN;
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( exception.getMessage(), exception );
        }
        else
        {
            log.info( exception.getMessage() );
        }

        ChangePasswordContext changepwContext = ( ChangePasswordContext ) context;
        ChangePasswordConfiguration config = changepwContext.getConfig();
        ChangePasswordException cpe = ( ChangePasswordException ) exception;

        ErrorMessage errorMessage = getErrorMessage( config.getChangepwPrincipal(), cpe );

        ChangePasswordErrorModifier modifier = new ChangePasswordErrorModifier();
        modifier.setErrorMessage( errorMessage );

        changepwContext.setReply( modifier.getChangePasswordError() );

        return STOP_CHAIN;
    }


    private ErrorMessage getErrorMessage( KerberosPrincipal principal, KerberosException exception )
    {
        ErrorMessageModifier modifier = new ErrorMessageModifier();

        KerberosTime now = new KerberosTime();

        modifier.setErrorCode( exception.getErrorCode() );
        modifier.setExplanatoryText( exception.getMessage() );
        modifier.setServerPrincipal( principal );
        modifier.setServerTime( now );
        modifier.setServerMicroSecond( 0 );
        modifier.setExplanatoryData( buildExplanatoryData( exception ) );

        return modifier.getErrorMessage();
    }


    private byte[] buildExplanatoryData( KerberosException exception )
    {
        short resultCode = ( short ) exception.getErrorCode();
        byte[] resultString = exception.getExplanatoryData();

        ByteBuffer byteBuffer = ByteBuffer.allocate( 256 );
        byteBuffer.putShort( resultCode );
        byteBuffer.put( resultString );

        byteBuffer.flip();
        byte[] explanatoryData = new byte[byteBuffer.remaining()];
        byteBuffer.get( explanatoryData, 0, explanatoryData.length );

        return explanatoryData;
    }
}
