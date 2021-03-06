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
package org.apache.directory.server.core.tools.schema;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.directory.server.core.tools.schema.AttributeTypeLiteral;
import org.apache.directory.server.core.tools.schema.ObjectClassLiteral;
import org.apache.directory.server.core.tools.schema.OpenLdapSchemaParser;
import org.apache.directory.shared.ldap.schema.ObjectClassTypeEnum;


/**
 * Tests the OpenLDAP schema parser.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class OpenLdapSchemaParserTest extends TestCase
{
    private OpenLdapSchemaParser parser;


    protected void setUp() throws Exception
    {
        super.setUp();

        parser = new OpenLdapSchemaParser();
        parser.setParserMonitor( new ConsoleParserMonitor() );
    }


    protected void tearDown() throws Exception
    {
        super.tearDown();
        parser = null;
    }


    public void testSimpleAttributeTypeNoLength() throws Exception
    {
        String attributeTypeData = "attributetype ( 2.5.4.14 NAME 'searchGuide'\n"
            + "        DESC 'RFC2256: search guide, obsoleted by enhancedSearchGuide'\n"
            + "        SYNTAX 1.3.6.1.4.1.1466.115.121.1.25 )";

        parser.parse( attributeTypeData );
        List attributeTypeList = parser.getAttributeTypes();
        Map attributeTypes = mapAttributeTypes( attributeTypeList );
        AttributeTypeLiteral type = ( AttributeTypeLiteral ) attributeTypes.get( "2.5.4.14" );

        assertNotNull( type );
        assertEquals( "2.5.4.14", type.getOid() );
        assertEquals( "searchGuide", type.getNames()[0] );
        assertEquals( "RFC2256: search guide, obsoleted by enhancedSearchGuide", type.getDescription() );
        assertEquals( "1.3.6.1.4.1.1466.115.121.1.25", type.getSyntax() );
    }


    private Map mapAttributeTypes( List attributeTypeList )
    {
        Map m = new HashMap();
        
        for ( int ii = 0 ; ii < attributeTypeList.size(); ii++ )
        {
            AttributeTypeLiteral type = ( AttributeTypeLiteral ) attributeTypeList.get( ii );
            m.put( type.getOid(), type );
        }
        
        return m;
    }


    public void testSimpleAttributeTypeParse() throws Exception
    {
        String attributeTypeData = "# adding a comment  \n" + "attributetype ( 2.5.4.2 NAME 'knowledgeInformation'\n"
            + "        DESC 'RFC2256: knowledge information'\n" + "        EQUALITY caseIgnoreMatch\n"
            + "        SYNTAX 1.3.6.1.4.1.1466.115.121.1.15{32768} )";
        parser.parse( attributeTypeData );
        List attributeTypeList = parser.getAttributeTypes();
        Map attributeTypes = mapAttributeTypes( attributeTypeList );
        AttributeTypeLiteral type = ( AttributeTypeLiteral ) attributeTypes.get( "2.5.4.2" );

        assertNotNull( type );
        assertEquals( "2.5.4.2", type.getOid() );
        assertEquals( "knowledgeInformation", type.getNames()[0] );
        assertEquals( "RFC2256: knowledge information", type.getDescription() );
        assertEquals( "1.3.6.1.4.1.1466.115.121.1.15", type.getSyntax() );
        assertEquals( 32768, type.getLength() );
    }


    public void testAttributeTypeParseWithDescQuotes() throws Exception
    {
        String attributeTypeData = "# adding a comment  \n" + "attributetype ( 2.5.4.2 NAME 'knowledgeInformation'\n"
            + "        DESC 'RFC2256: \"knowledge\" information'\n" + "        EQUALITY caseIgnoreMatch\n"
            + "        SYNTAX 1.3.6.1.4.1.1466.115.121.1.15{32768} )";
        parser.parse( attributeTypeData );
        List attributeTypeList = parser.getAttributeTypes();
        Map attributeTypes = mapAttributeTypes( attributeTypeList );
        AttributeTypeLiteral type = ( AttributeTypeLiteral ) attributeTypes.get( "2.5.4.2" );

        assertNotNull( type );
        assertEquals( "2.5.4.2", type.getOid() );
        assertEquals( "knowledgeInformation", type.getNames()[0] );
        assertEquals( "RFC2256: \\\"knowledge\\\" information", type.getDescription() );
        assertEquals( "1.3.6.1.4.1.1466.115.121.1.15", type.getSyntax() );
        assertEquals( 32768, type.getLength() );
    }


    public void testComplexAttributeTypeParse() throws Exception
    {
        String attributeTypeData = "# adding a comment  \n"
            + "attributetype ( 2.5.4.2 NAME ( 'knowledgeInformation' 'asdf' ) \n"
            + "        DESC 'RFC2256: knowledge information'\n" + "        EQUALITY caseIgnoreMatch\n"
            + "        SYNTAX 1.3.6.1.4.1.1466.115.121.1.15{32768} )";
        parser.parse( attributeTypeData );
        List attributeTypeList = parser.getAttributeTypes();
        Map attributeTypes = mapAttributeTypes( attributeTypeList );
        AttributeTypeLiteral type = ( AttributeTypeLiteral ) attributeTypes.get( "2.5.4.2" );

        assertNotNull( type );
        assertEquals( "2.5.4.2", type.getOid() );
        assertEquals( "knowledgeInformation", type.getNames()[0] );
        assertEquals( "RFC2256: knowledge information", type.getDescription() );
        assertEquals( "1.3.6.1.4.1.1466.115.121.1.15", type.getSyntax() );
        assertEquals( 32768, type.getLength() );
    }


    private Map mapObjectClasses( List attributeTypeList )
    {
        Map m = new HashMap();
        
        for ( int ii = 0 ; ii < attributeTypeList.size(); ii++ )
        {
            ObjectClassLiteral type = ( ObjectClassLiteral ) attributeTypeList.get( ii );
            m.put( type.getOid(), type );
        }
        
        return m;
    }


    public void testObjectClassParse() throws Exception
    {
        String objectClassData = "objectclass ( 2.5.6.6 NAME 'person'\n" + "        DESC 'RFC2256: a person'\n"
            + "        SUP top STRUCTURAL\n" + "        MUST ( sn $ cn )\n"
            + "        MAY ( userPassword $ telephoneNumber $ seeAlso $ description ) )";
        parser.parse( objectClassData );
        List objectClassesList = parser.getObjectClassTypes();
        Map objectClasses = mapObjectClasses( objectClassesList );
        ObjectClassLiteral objectClass = ( ObjectClassLiteral ) objectClasses.get( "2.5.6.6" );

        assertNotNull( objectClass );
        assertEquals( "2.5.6.6", objectClass.getOid() );
        assertEquals( "person", objectClass.getNames()[0] );
        assertEquals( "RFC2256: a person", objectClass.getDescription() );
        assertEquals( ObjectClassTypeEnum.STRUCTURAL, objectClass.getClassType() );
        assertEquals( "sn", objectClass.getMust()[0] );
        assertEquals( "cn", objectClass.getMust()[1] );
        assertEquals( "userPassword", objectClass.getMay()[0] );
        assertEquals( "telephoneNumber", objectClass.getMay()[1] );
        assertEquals( "seeAlso", objectClass.getMay()[2] );
        assertEquals( "description", objectClass.getMay()[3] );
    }


    public void testObjectClassMultipleNames() throws Exception
    {
        String objectClassData = "objectclass ( 0.9.2342.19200300.100.4.4\n"
            + "\tNAME ( 'pilotPerson' 'newPilotPerson' )\n" + "\tSUP person STRUCTURAL\n"
            + "\tMAY ( userid $ textEncodedORAddress $ rfc822Mailbox $\n"
            + "\t\tfavouriteDrink $ roomNumber $ userClass $\n"
            + "\t\thomeTelephoneNumber $ homePostalAddress $ secretary $\n"
            + "\t\tpersonalTitle $ preferredDeliveryMethod $ businessCategory $\n"
            + "\t\tjanetMailbox $ otherMailbox $ mobileTelephoneNumber $\n"
            + "\t\tpagerTelephoneNumber $ organizationalStatus $\n"
            + "\t\tmailPreferenceOption $ personalSignature )\n" + "\t)";
        parser.parse( objectClassData );
        List objectClassesList = parser.getObjectClassTypes();
        Map objectClasses = mapObjectClasses( objectClassesList );
        ObjectClassLiteral objectClass = ( ObjectClassLiteral ) objectClasses.get( "0.9.2342.19200300.100.4.4" );

        assertNotNull( objectClass );
        assertEquals( "0.9.2342.19200300.100.4.4", objectClass.getOid() );
        assertEquals( "pilotPerson", objectClass.getNames()[0] );
        assertEquals( "newPilotPerson", objectClass.getNames()[1] );
        assertEquals( ObjectClassTypeEnum.STRUCTURAL, objectClass.getClassType() );
        assertEquals( "person", objectClass.getSuperiors()[0] );

        assertEquals( "userid", objectClass.getMay()[0] );
        assertEquals( "textEncodedORAddress", objectClass.getMay()[1] );
        assertEquals( "rfc822Mailbox", objectClass.getMay()[2] );
        assertEquals( "favouriteDrink", objectClass.getMay()[3] );
        assertEquals( "roomNumber", objectClass.getMay()[4] );
        assertEquals( "userClass", objectClass.getMay()[5] );
        assertEquals( "homeTelephoneNumber", objectClass.getMay()[6] );
        assertEquals( "homePostalAddress", objectClass.getMay()[7] );
        assertEquals( "secretary", objectClass.getMay()[8] );
        assertEquals( "personalTitle", objectClass.getMay()[9] );
        assertEquals( "preferredDeliveryMethod", objectClass.getMay()[10] );
        assertEquals( "businessCategory", objectClass.getMay()[11] );
        assertEquals( "janetMailbox", objectClass.getMay()[12] );
        assertEquals( "otherMailbox", objectClass.getMay()[13] );
        assertEquals( "mobileTelephoneNumber", objectClass.getMay()[14] );
        assertEquals( "pagerTelephoneNumber", objectClass.getMay()[15] );
        assertEquals( "organizationalStatus", objectClass.getMay()[16] );
        assertEquals( "mailPreferenceOption", objectClass.getMay()[17] );
        assertEquals( "personalSignature", objectClass.getMay()[18] );

    }
}
