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
package org.apache.directory.server;


import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.SchemaViolationException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.LDAPAttributeSet;
import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPEntry;
import netscape.ldap.LDAPException;

import org.apache.directory.server.unit.AbstractServerTest;
import org.apache.directory.shared.ldap.message.LockableAttributeImpl;
import org.apache.directory.shared.ldap.message.LockableAttributesImpl;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Hashtable;


/**
 * Various add scenario tests.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class AddITest extends AbstractServerTest
{
    private static final String RDN = "cn=The Person";

    private DirContext ctx = null;


    /**
     * Create an entry for a person.
     */
    public void setUp() throws Exception
    {
        super.setUp();

        Hashtable env = new Hashtable();
        env.put( "java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory" );
        env.put( "java.naming.provider.url", "ldap://localhost:" + port + "/ou=system" );
        env.put( "java.naming.security.principal", "uid=admin,ou=system" );
        env.put( "java.naming.security.credentials", "secret" );
        env.put( "java.naming.security.authentication", "simple" );
        ctx = new InitialDirContext( env );

        // Create a person
        Attributes attributes = new LockableAttributesImpl( true );
        Attribute attribute = new LockableAttributeImpl( "objectClass" );
        attribute.add( "top" );
        attribute.add( "person" );
        attributes.put( attribute );
        attributes.put( "cn", "The Person" );
        attributes.put( "sn", "Person" );
        attributes.put( "description", "this is a person" );
        attributes.put(  "administrativeRole", "subschemaAdminSpecificArea" );
        DirContext person = ctx.createSubcontext( RDN, attributes );

        assertNotNull( person );
    }


    /**
     * Remove the person.
     */
    public void tearDown() throws Exception
    {
        ctx.unbind( RDN );
        ctx.close();
        ctx = null;
        super.tearDown();
    }


    /**
     * Just a little test to check wether the person is created correctly after
     * setup.
     * 
     * @throws NamingException
     */
    public void testSetUpTearDown() throws NamingException
    {
        DirContext person = ( DirContext ) ctx.lookup( RDN );
        assertNotNull( person );

        // Check object classes

        Attributes attributes = person.getAttributes( "" );
        Attribute ocls = attributes.get( "objectClass" );

        String[] expectedOcls =
            { "top", "person" };
        for ( int i = 0; i < expectedOcls.length; i++ )
        {
            String name = expectedOcls[i];
            assertTrue( "object class " + name + " is NOT present when it should be!", ocls.contains( name ) );
        }
    }


    /**
     * This is the original defect as in JIRA DIREVE-216.
     * 
     * @throws NamingException
     */
    public void testAddObjectClasses() throws NamingException
    {

        // modify object classes, add two more
        Attributes attributes = new LockableAttributesImpl( true );
        Attribute ocls = new LockableAttributeImpl( "objectClass" );
        ocls.add( "organizationalPerson" );
        ocls.add( "inetOrgPerson" );
        attributes.put( ocls );

        DirContext person = ( DirContext ) ctx.lookup( RDN );
        person.modifyAttributes( "", DirContext.ADD_ATTRIBUTE, attributes );

        // Read again from directory
        person = ( DirContext ) ctx.lookup( RDN );
        attributes = person.getAttributes( "" );
        Attribute newOcls = attributes.get( "objectClass" );

        String[] expectedOcls =
            { "top", "person", "organizationalPerson", "inetOrgPerson" };
        for ( int i = 0; i < expectedOcls.length; i++ )
        {
            String name = expectedOcls[i];
            assertTrue( "object class " + name + " is present", newOcls.contains( name ) );
        }
    }


    /**
     * This changes a single attribute value. Just as a reference.
     * 
     * @throws NamingException
     */
    public void testModifyDescription() throws NamingException
    {
        String newDescription = "More info on the user ...";

        // modify object classes, add two more
        Attributes attributes = new LockableAttributesImpl( true );
        Attribute desc = new LockableAttributeImpl( "description", newDescription );
        attributes.put( desc );

        DirContext person = ( DirContext ) ctx.lookup( RDN );
        person.modifyAttributes( "", DirContext.REPLACE_ATTRIBUTE, attributes );

        // Read again from directory
        person = ( DirContext ) ctx.lookup( RDN );
        attributes = person.getAttributes( "" );
        Attribute newDesc = attributes.get( "description" );

        assertTrue( "new Description", newDesc.contains( newDescription ) );
    }


    /**
     * Try to add entry with required attribute missing.
     */
    public void testAddWithMissingRequiredAttributes() throws NamingException
    {
        // person without sn
        Attributes attrs = new LockableAttributesImpl();
        Attribute ocls = new LockableAttributeImpl( "objectClass" );
        ocls.add( "top" );
        ocls.add( "person" );
        attrs.put( ocls );
        attrs.put( "cn", "Fiona Apple" );

        try
        {
            ctx.createSubcontext( "cn=Fiona Apple", attrs );
            fail( "creation of entry should fail" );
        }
        catch ( SchemaViolationException e )
        {
            // expected
        }
    }
    
    

    static final String HOST = "localhost";
    static final String USER = "uid=admin,ou=system";
    static final String PASSWORD = "secret";
    static final String BASE = "ou=system";


    /**
     * Testcase to demonstrate DIRSERVER-643 ("Netscape SDK: Adding an entry with
     * two description attributes does not combine values."). Uses Sun ONE Directory
     * SDK for Java 4.1 , or comparable (Netscape, Mozilla).
     */
    public void testAddEntryWithTwoDescriptions() throws LDAPException
    {
        LDAPConnection con = new LDAPConnection();
        con.connect( 3, HOST, super.port, USER, PASSWORD );
        LDAPAttributeSet attrs = new LDAPAttributeSet();
        LDAPAttribute ocls = new LDAPAttribute( "objectclass", new String[]
            { "top", "person" } );
        attrs.add( ocls );
        attrs.add( new LDAPAttribute( "sn", "Bush" ) );
        attrs.add( new LDAPAttribute( "cn", "Kate Bush" ) );

        String descr[] =
            { "a British singer-songwriter with an expressive four-octave voice",
                "one of the most influential female artists of the twentieth century" };

        attrs.add( new LDAPAttribute( "description", descr ) );

        String dn = "cn=Kate Bush," + BASE;
        LDAPEntry kate = new LDAPEntry( dn, attrs );

        con.add( kate );

        // Analyze entry and description attribute
        LDAPEntry kateReloaded = con.read( dn );
        assertNotNull( kateReloaded );
        LDAPAttribute attr = kateReloaded.getAttribute( "description" );
        assertNotNull( attr );
        assertEquals( 2, attr.getStringValueArray().length );

        // Remove entry
        con.delete( dn );
        con.disconnect();
    }


    /**
     * Testcase to demonstrate DIRSERVER-643 ("Netscape SDK: Adding an entry with
     * two description attributes does not combine values."). Uses Sun ONE Directory
     * SDK for Java 4.1 , or comparable (Netscape, Mozilla).
     */
    public void testAddEntryWithTwoDescriptionsVariant() throws LDAPException
    {
        LDAPConnection con = new LDAPConnection();
        con.connect( 3, HOST, super.port, USER, PASSWORD );
        LDAPAttributeSet attrs = new LDAPAttributeSet();
        LDAPAttribute ocls = new LDAPAttribute( "objectclass", new String[]
            { "top", "person" } );
        attrs.add( ocls );
        attrs.add( new LDAPAttribute( "sn", "Bush" ) );
        attrs.add( new LDAPAttribute( "cn", "Kate Bush" ) );

        String descr[] =
            { "a British singer-songwriter with an expressive four-octave voice",
                "one of the most influential female artists of the twentieth century" };

        attrs.add( new LDAPAttribute( "description", descr[0] ) );
        attrs.add( new LDAPAttribute( "description", descr[1] ) );

        String dn = "cn=Kate Bush," + BASE;
        LDAPEntry kate = new LDAPEntry( dn, attrs );

        con.add( kate );

        // Analyze entry and description attribute
        LDAPEntry kateReloaded = con.read( dn );
        assertNotNull( kateReloaded );
        LDAPAttribute attr = kateReloaded.getAttribute( "description" );
        assertNotNull( attr );
        assertEquals( 2, attr.getStringValueArray().length );

        // Remove entry
        con.delete( dn );
        con.disconnect();
    }


    /**
     * Testcase to demonstrate DIRSERVER-643 ("Netscape SDK: Adding an entry with
     * two description attributes does not combine values."). Uses Sun ONE Directory
     * SDK for Java 4.1 , or comparable (Netscape, Mozilla).
     */
    public void testAddEntryWithTwoDescriptionsSecondVariant() throws LDAPException
    {
        LDAPConnection con = new LDAPConnection();
        con.connect( 3, HOST, super.port, USER, PASSWORD );
        LDAPAttributeSet attrs = new LDAPAttributeSet();
        LDAPAttribute ocls = new LDAPAttribute( "objectclass", new String[]
            { "top", "person" } );
        attrs.add( ocls );
        attrs.add( new LDAPAttribute( "sn", "Bush" ) );

        String descr[] =
            { "a British singer-songwriter with an expressive four-octave voice",
                "one of the most influential female artists of the twentieth century" };

        attrs.add( new LDAPAttribute( "description", descr[0] ) );
        attrs.add( new LDAPAttribute( "cn", "Kate Bush" ) );
        attrs.add( new LDAPAttribute( "description", descr[1] ) );

        String dn = "cn=Kate Bush," + BASE;
        LDAPEntry kate = new LDAPEntry( dn, attrs );

        con.add( kate );

        // Analyze entry and description attribute
        LDAPEntry kateReloaded = con.read( dn );
        assertNotNull( kateReloaded );
        LDAPAttribute attr = kateReloaded.getAttribute( "description" );
        assertNotNull( attr );
        assertEquals( 2, attr.getStringValueArray().length );

        // Remove entry
        con.delete( dn );
        con.disconnect();
    }
    
    /**
     * Try to add entry with invalid number of values for a single-valued atribute
     * @see http://issues.apache.org/jira/browse/DIRSERVER-614
     */
    public void testAddWithInvalidNumberOfAttributeValues() throws NamingException
    {
        // add inetOrgPerson with two displayNames
        Attributes attrs = new LockableAttributesImpl();
        Attribute ocls = new LockableAttributeImpl( "objectClass" );
        ocls.add( "top" );
        ocls.add( "inetOrgPerson" );
        attrs.put( ocls );
        attrs.put( "cn", "Fiona Apple" );
        attrs.put( "sn", "Apple" );
        Attribute displayName = new LockableAttributeImpl( "displayName" );
        displayName.add( "Fiona" );
        displayName.add( "Fiona A." );
        attrs.put( displayName );

        try
        {
            ctx.createSubcontext( "cn=Fiona Apple", attrs );
            fail( "creation of entry should fail" );
        }
        catch ( InvalidAttributeValueException e )
        {
            
        }
    }


    /**
     * Try to add entry and an alias to it. Afterwards, remove it.
     */
    public void testAddAlias() throws NamingException
    {

        // Create entry
        Attributes entry = new LockableAttributesImpl();
        Attribute entryOcls = new LockableAttributeImpl( "objectclass" );
        entryOcls.add( "top" );
        entryOcls.add( "organizationalUnit" );
        entry.put( entryOcls );
        entry.put( "ou", "favorite" );
        String entryRdn = "ou=favorite";
        ctx.createSubcontext( entryRdn, entry );

        // Create Alias
        String aliasedObjectName = entryRdn + "," + ctx.getNameInNamespace();
        Attributes alias = new LockableAttributesImpl();
        Attribute aliasOcls = new LockableAttributeImpl( "objectclass" );
        aliasOcls.add( "top" );
        aliasOcls.add( "organizationalUnit" );
        aliasOcls.add( "alias" );
        alias.put( aliasOcls );
        alias.put( "ou", "bestFruit" );
        alias.put( "aliasedObjectName", aliasedObjectName );
        String rdnAlias = "ou=bestFruit";
        ctx.createSubcontext( rdnAlias, alias );

        // Remove alias and entry
        ctx.destroySubcontext( rdnAlias );
        ctx.destroySubcontext( entryRdn );
    }


    /**
     * Try to add entry and an alias to it. Afterwards, remove it. This version
     * cretes a container entry before the operations.
     */
    public void testAddAliasInContainer() throws NamingException
    {

        // Create container
        Attributes container = new LockableAttributesImpl();
        Attribute containerOcls = new LockableAttributeImpl( "objectclass" );
        containerOcls.add( "top" );
        containerOcls.add( "organizationalUnit" );
        container.put( containerOcls );
        container.put( "ou", "Fruits" );
        String containerRdn = "ou=Fruits";
        DirContext containerCtx = ctx.createSubcontext( containerRdn, container );

        // Create entry
        Attributes entry = new LockableAttributesImpl();
        Attribute entryOcls = new LockableAttributeImpl( "objectclass" );
        entryOcls.add( "top" );
        entryOcls.add( "organizationalUnit" );
        entry.put( entryOcls );
        entry.put( "ou", "favorite" );
        String entryRdn = "ou=favorite";
        containerCtx.createSubcontext( entryRdn, entry );

        // Create alias ou=bestFruit,ou=Fruits to entry ou=favorite,ou=Fruits
        String aliasedObjectName = entryRdn + "," + containerCtx.getNameInNamespace();
        Attributes alias = new LockableAttributesImpl();
        Attribute aliasOcls = new LockableAttributeImpl( "objectclass" );
        aliasOcls.add( "top" );
        aliasOcls.add( "organizationalUnit" );
        aliasOcls.add( "alias" );
        alias.put( aliasOcls );
        alias.put( "ou", "bestFruit" );
        alias.put( "aliasedObjectName", aliasedObjectName );
        String rdnAlias = "ou=bestFruit";
        containerCtx.createSubcontext( rdnAlias, alias );

        // search one level scope for alias 
        SearchControls controls = new SearchControls();
        controls.setDerefLinkFlag( true );
        controls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
        containerCtx.addToEnvironment( "java.naming.ldap.derefAliases", "never" );
        NamingEnumeration ne = containerCtx.search( "", "(objectClass=*)", controls );
        assertTrue( ne.hasMore() );
        SearchResult sr = ( SearchResult ) ne.next();
        assertEquals( "ou=favorite", sr.getName() );
        assertTrue( ne.hasMore() );
        sr = ( SearchResult ) ne.next();
        assertEquals( "ou=bestFruit", sr.getName() );
        
        // search one level with dereferencing turned on
        controls = new SearchControls();
        controls.setDerefLinkFlag( true );
        controls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
        containerCtx.addToEnvironment( "java.naming.ldap.derefAliases", "always" );
        ne = containerCtx.search( "", "(objectClass=*)", controls );
        assertTrue( ne.hasMore() );
        sr = ( SearchResult ) ne.next();
        assertEquals( "ou=favorite", sr.getName() );
        assertFalse( ne.hasMore() );
        
        // search with base set to alias and dereferencing turned on
        controls = new SearchControls();
        controls.setDerefLinkFlag( false );
        controls.setSearchScope( SearchControls.OBJECT_SCOPE );
        containerCtx.addToEnvironment( "java.naming.ldap.derefAliases", "always" );
        ne = containerCtx.search( "ou=bestFruit", "(objectClass=*)", controls );
        assertTrue( ne.hasMore() );
        sr = ( SearchResult ) ne.next();
        assertTrue( sr.getName().endsWith( "ou=favorite,ou=Fruits,ou=system" ) );
        assertFalse( ne.hasMore() );
        
        // Remove alias and entry
        containerCtx.destroySubcontext( rdnAlias );
        containerCtx.destroySubcontext( entryRdn );

        // Remove container
        ctx.destroySubcontext( containerRdn );
    }

    /**
     * Testcase to demonstrate DIRSERVER-643 ("Netscape SDK: Adding an entry with
     * two description attributes does not combine values."). Uses Sun ONE Directory
     * SDK for Java 4.1 , or comparable (Netscape, Mozilla).
     */
    public void testAddEntryWithACIAndTabs() throws LDAPException
    {
        LDAPConnection con = new LDAPConnection();
        con.connect( 3, HOST, super.port, USER, PASSWORD );
        LDAPAttributeSet attrs = new LDAPAttributeSet();
        LDAPAttribute ocls = new LDAPAttribute( "objectclass", new String[]
            { "top", "subentry", "accessControlSubentry" } );
        attrs.add( ocls );
        attrs.add( new LDAPAttribute( "subtreeSpecification", "{}" ) );
        attrs.add( new LDAPAttribute( "prescriptiveACI", 
            "{" +
            "   identificationTag \"allowJohnToReadHisName_ACI13\", \n" +
            "   precedence 10, \n" + 
            "   authenticationLevel simple,\n" +
            "   itemOrUserFirst userFirst: \n" +
            "   {\n" +
            "       userClasses\n" + 
            "       {\n" +
            "           name { \"cn=John,cn=The Person, ou=systemm\" }\n" +
            "       },\n" +
            "       userPermissions \n" +
            "       {\n" +
            "           { \n" +
            "               protectedItems { entry },\n" +
            "               grantsAndDenials { grantBrowse }\n" +
            "           },\n" +
            "           { \n" +
            "               protectedItems\n" +
            "               {\n" +
            "                   attributeType { cn },\n" +
            "                   allAttributeValues { cn }\n" +
            "               },\n" +
            "               grantsAndDenials { grantRead }\n" +
            "           }\n" +
            "       }\n" +
            "   }\n" +
            "}" ) );
        
        attrs.add( new LDAPAttribute( "cn", "allowJohnToReadHisName_ACI13" ) );

        String dn = "cn=allowJohnToReadHisName_ACI13,cn=The person," + BASE;

        LDAPEntry entry = new LDAPEntry( dn, attrs );

        con.add( entry );

        // Analyze entry and description attribute
        LDAPEntry entryReloaded = con.read( dn );
        assertNotNull( entryReloaded );

        // Remove entry
        con.delete( dn );
        con.disconnect();
    }
    
    /**
     * Test that attribute name case is preserved after adding an entry
     * in the case the user added them.  This is to test DIRSERVER-832.
     */
    public void testAddCasePreservedOnAttributeNames() throws Exception
    {
        Attributes attrs = new LockableAttributesImpl( true );
        Attribute oc = new LockableAttributeImpl( "ObjectClass", "top" );
        oc.add( "PERSON" );
        oc.add( "organizationalPerson" );
        oc.add( "inetORGperson" );
        Attribute cn = new LockableAttributeImpl( "Cn", "Kevin Spacey" );
        Attribute dc = new LockableAttributeImpl( "sN", "Spacey" );
        attrs.put( oc );
        attrs.put( cn );
        attrs.put( dc);
        sysRoot.createSubcontext( "uID=kevin", attrs );
        Attributes returned = sysRoot.getAttributes( "UID=kevin" );
        
        NamingEnumeration attrList = returned.getAll();
        while( attrList.hasMore() )
        {
            Attribute attr = ( Attribute ) attrList.next();
            
            if ( attr.getID().equalsIgnoreCase( "uid" ) )
            {
                assertEquals( "uID", attr.getID() );
            }
            
            if ( attr.getID().equalsIgnoreCase( "objectClass" ) )
            {
                assertEquals( "objectClass", attr.getID() );
            }
            
            if ( attr.getID().equalsIgnoreCase( "sn" ) )
            {
                assertEquals( "sN", attr.getID() );
            }
            
            if ( attr.getID().equalsIgnoreCase( "cn" ) )
            {
                assertEquals( "Cn", attr.getID() );
            }
        }
    }
}
