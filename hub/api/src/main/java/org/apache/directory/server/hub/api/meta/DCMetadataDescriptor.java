package org.apache.directory.server.hub.api.meta;


import java.util.Hashtable;

import org.apache.directory.server.hub.api.component.DirectoryComponentConstants;
import org.osgi.framework.Version;


public class DCMetadataDescriptor
{
    private String metadataPID;
    private Version metaVersion;

    private String className = "";
    private String[] implemented = new String[0];
    private String[] extended = new String[0];

    private DCPropertyDescription[] properties;

    private Hashtable<String, DCPropertyDescription> propertyMap;

    private boolean factory;


    public DCMetadataDescriptor(
        String metadataPID,
        boolean isFactory,
        Version metaVersion,
        String className,
        String[] implemented,
        String[] extended,
        DCPropertyDescription[] properties )
    {
        this.metadataPID = metadataPID;
        this.factory = isFactory;
        this.metaVersion = metaVersion;
        this.className = className;
        this.implemented = implemented;
        this.extended = extended;
        this.properties = properties;

        propertyMap = new Hashtable<String, DCPropertyDescription>();

        if ( properties != null )
        {
            for ( DCPropertyDescription p : properties )
            {
                propertyMap.put( p.getName(), p );
            }
        }
    }


    public String getMetadataPID()
    {
        return metadataPID;
    }


    public boolean isFactory()
    {
        return factory;
    }


    public Version getMetaVersion()
    {
        return metaVersion;
    }


    public String getClassName()
    {
        return className;
    }


    public String[] getExtendedClasses()
    {
        return extended;
    }


    public String[] getImplementedInterfaces()
    {
        return implemented;
    }


    public DCPropertyDescription[] getPropertyDescriptons()
    {
        return properties;
    }


    public DCPropertyDescription getPropertyDescription( String propertyName )
    {
        if ( propertyName.startsWith( DirectoryComponentConstants.DC_PROP_ITEM_PREFIX ) )
        {
            return DirectoryComponentConstants.itemDescription;
        }

        return propertyMap.get( propertyName );
    }


    public boolean compatibleWith( DCMetadataDescriptor metadata )
    {
        Version version = metadata.getMetaVersion();

        if ( version.getQualifier().contains( "SNAPSHOT" ) || metaVersion.getQualifier().contains( "SNAPSHOT" ) )
        {
            return false;
        }

        if ( metaVersion.compareTo( version ) == 0 )
        {
            return true;
        }
        else
        {
            if ( metaVersion.getMajor() == version.getMajor() )
            {
                if ( metaVersion.getMinor() == version.getMinor() )
                {
                    return true;
                }
            }
        }

        return false;
    }


    public boolean is( String type )
    {
        if ( className.equals( type ) )
        {
            return true;
        }

        for ( String sClass : extended )
        {
            if ( sClass.equals( type ) )
            {
                return true;
            }
        }

        for ( String iface : implemented )
        {
            if ( iface.equals( type ) )
            {
                return true;
            }
        }

        return false;
    }

}