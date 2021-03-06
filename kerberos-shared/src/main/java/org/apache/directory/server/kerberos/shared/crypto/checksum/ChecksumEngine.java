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
package org.apache.directory.server.kerberos.shared.crypto.checksum;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.directory.server.kerberos.shared.crypto.encryption.CipherType;


public abstract class ChecksumEngine
{
    public abstract MessageDigest getDigest() throws NoSuchAlgorithmException;
    
    public abstract ChecksumType checksumType();


    public abstract CipherType keyType();


    public abstract int checksumSize();


    public abstract int keySize();


    public abstract int confounderSize();


    public abstract boolean isSafe();


    public abstract byte[] calculateKeyedChecksum( byte[] data, byte[] key );


    public abstract boolean verifyKeyedChecksum( byte[] data, byte[] key, byte[] checksum );


    public byte[] calculateChecksum( byte[] data )
    {
        try
        {
            MessageDigest digester = getDigest();
            return digester.digest( data );
        }
        catch ( NoSuchAlgorithmException nsae )
        {
            return null;
        }
    }
}
