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

package org.apache.directory.server.dhcp.io;


import java.nio.ByteBuffer;

import org.apache.directory.server.dhcp.options.DhcpOption;
import org.apache.directory.server.dhcp.options.OptionsField;
import org.apache.directory.server.dhcp.options.vendor.EndOption;


public class DhcpOptionsEncoder
{
    private static final byte[] VENDOR_MAGIC_COOKIE =
        { ( byte ) 99, ( byte ) 130, ( byte ) 83, ( byte ) 99 };


    public void encode( OptionsField options, ByteBuffer message )
    {
        message.put( VENDOR_MAGIC_COOKIE );

        DhcpOption[] optionsArray = options.toArray();

        for ( int ii = 0; ii < optionsArray.length; ii++ )
        {
            optionsArray[ii].writeTo( message );
        }

        DhcpOption endOption = new EndOption();
        endOption.writeTo( message );
    }
}
