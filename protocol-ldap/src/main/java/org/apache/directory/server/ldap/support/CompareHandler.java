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
package org.apache.directory.server.ldap.support;


import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ReferralException;
import javax.naming.ldap.LdapContext;

import org.apache.directory.server.core.configuration.StartupConfiguration;
import org.apache.directory.server.core.jndi.ServerLdapContext;
import org.apache.directory.server.ldap.SessionRegistry;
import org.apache.directory.shared.ldap.exception.LdapException;
import org.apache.directory.shared.ldap.message.CompareRequest;
import org.apache.directory.shared.ldap.message.Control;
import org.apache.directory.shared.ldap.message.LdapResult;
import org.apache.directory.shared.ldap.message.ManageDsaITControl;
import org.apache.directory.shared.ldap.message.ReferralImpl;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.directory.shared.ldap.util.ExceptionUtils;

import org.apache.mina.common.IoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A single reply handler for {@link org.apache.directory.shared.ldap.message.CompareRequest}s.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class CompareHandler implements LdapMessageHandler
{
    private static final Logger log = LoggerFactory.getLogger( CompareHandler.class );
    private static Control[] EMPTY_CONTROLS = new Control[0];

    /** Speedup for logs */
    private static final boolean IS_DEBUG = log.isDebugEnabled();

    public void messageReceived( IoSession session, Object request )
    {
        CompareRequest req = ( CompareRequest ) request;
        LdapResult result = req.getResultResponse().getLdapResult();

        try
        {
            LdapContext ctx = SessionRegistry.getSingleton().getLdapContext( session, null, true );
            ServerLdapContext newCtx = ( ServerLdapContext ) ctx.lookup( "" );
            
            if ( req.getControls().containsKey( ManageDsaITControl.CONTROL_OID ) )
            {
                newCtx.addToEnvironment( Context.REFERRAL, "ignore" );
            }
            else
            {
                newCtx.addToEnvironment( Context.REFERRAL, "throw" );
            }
            newCtx.setRequestControls( ( Control[] ) req.getControls().values().toArray( EMPTY_CONTROLS ) );

            if ( newCtx.compare( req.getName(), req.getAttributeId(), req.getAssertionValue() ) )
            {
                result.setResultCode( ResultCodeEnum.COMPARETRUE );
            }
            else
            {
                result.setResultCode( ResultCodeEnum.COMPAREFALSE );
            }
        }
        catch ( ReferralException e )
        {
            ReferralImpl refs = new ReferralImpl();
            result.setReferral( refs );
            result.setResultCode( ResultCodeEnum.REFERRAL );
            result.setErrorMessage( "Encountered referral attempting to handle compare request." );

            result.setMatchedDn( (LdapDN)e.getResolvedName() );
            
            do
            {
                refs.addLdapUrl( ( String ) e.getReferralInfo() );
            }
            while ( e.skipReferral() );
            
            session.write( req.getResultResponse() );
            return;
        }
        catch ( Exception e )
        {
            String msg = "failed to compare entry " + req.getName() + ": " + e.getMessage();

            if ( IS_DEBUG )
            {
                msg += ":\n" + ExceptionUtils.getStackTrace( e );
            }

            ResultCodeEnum code;

            if ( e instanceof LdapException )
            {
                code = ( ( LdapException ) e ).getResultCode();
            }
            else
            {
                code = ResultCodeEnum.getBestEstimate( e, req.getType() );
            }

            result.setResultCode( code );
            result.setErrorMessage( msg );

            if ( e instanceof NamingException )
            {
                NamingException ne = ( NamingException ) e;

                if ( ( ne.getResolvedName() != null )
                    && ( ( code == ResultCodeEnum.NOSUCHOBJECT ) || ( code == ResultCodeEnum.ALIASPROBLEM )
                        || ( code == ResultCodeEnum.INVALIDDNSYNTAX ) || ( code == ResultCodeEnum.ALIASDEREFERENCINGPROBLEM ) ) )
                {
                    result.setMatchedDn( (LdapDN)ne.getResolvedName() );
                }
            }

            session.write( req.getResultResponse() );
            return;
        }

        result.setMatchedDn( req.getName() );
        session.write( req.getResultResponse() );
    }


    public void init( StartupConfiguration cfg )
    {
    }
}
