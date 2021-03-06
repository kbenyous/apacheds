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
package org.apache.directory.server.core.authz.support;


import java.util.Collection;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.directory.server.core.partition.PartitionNexusProxy;
import org.apache.directory.shared.ldap.aci.ACITuple;
import org.apache.directory.shared.ldap.aci.AuthenticationLevel;
import org.apache.directory.shared.ldap.aci.ProtectedItem;
import org.apache.directory.shared.ldap.aci.ProtectedItem.MaxValueCountItem;
import org.apache.directory.shared.ldap.name.LdapDN;


/**
 * An {@link ACITupleFilter} that discards all tuples that doesn't satisfy
 * {@link ProtectedItem.MaxValueCount} constraint if available. (18.8.3.3, X.501)
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class MaxValueCountFilter implements ACITupleFilter
{
    public Collection filter( Collection tuples, OperationScope scope, PartitionNexusProxy proxy,
                              Collection userGroupNames, LdapDN userName, Attributes userEntry, AuthenticationLevel authenticationLevel,
                              LdapDN entryName, String attrId, Object attrValue, Attributes entry, Collection microOperations, Attributes entryView )
        throws NamingException
    {
        if ( scope != OperationScope.ATTRIBUTE_TYPE_AND_VALUE )
        {
            return tuples;
        }

        if ( tuples.size() == 0 )
        {
            return tuples;
        }

        for ( Iterator i = tuples.iterator(); i.hasNext(); )
        {
            ACITuple tuple = ( ACITuple ) i.next();
            if ( !tuple.isGrant() )
            {
                continue;
            }

            for ( Iterator j = tuple.getProtectedItems().iterator(); j.hasNext(); )
            {
                ProtectedItem item = ( ProtectedItem ) j.next();
                if ( item instanceof ProtectedItem.MaxValueCount )
                {
                    ProtectedItem.MaxValueCount mvc = ( ProtectedItem.MaxValueCount ) item;
                    if ( isRemovable( mvc, attrId, entryView ) )
                    {
                        i.remove();
                        break;
                    }
                }
            }
        }

        return tuples;
    }


    private boolean isRemovable( ProtectedItem.MaxValueCount mvc, String attrId, Attributes entry )
    {
        for ( Iterator k = mvc.iterator(); k.hasNext(); )
        {
            MaxValueCountItem mvcItem = ( MaxValueCountItem ) k.next();
            if ( attrId.equalsIgnoreCase( mvcItem.getAttributeType() ) )
            {
                Attribute attr = entry.get( attrId );
                int attrCount = attr == null ? 0 : attr.size();
                if ( attrCount > mvcItem.getMaxCount() )
                {
                    return true;
                }
            }
        }

        return false;
    }

}
