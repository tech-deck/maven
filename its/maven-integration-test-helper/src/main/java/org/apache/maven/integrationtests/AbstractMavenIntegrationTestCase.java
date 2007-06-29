package org.apache.maven.integrationtests;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.it.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

/**
 * @author Jason van Zyl
 * @author Kenney Westerhof
 */
public abstract class AbstractMavenIntegrationTestCase
    extends TestCase
{
    /**
     * Save System.out for progress reports etc.
     */
    private static PrintStream out = System.out;

    private boolean skip;

    private DefaultArtifactVersion version;

    private VersionRange versionRange;

    protected AbstractMavenIntegrationTestCase()
    {
    }

    protected AbstractMavenIntegrationTestCase( String versionRangeStr )
        throws InvalidVersionSpecificationException
    {
        this.versionRange = VersionRange.createFromVersionSpec( versionRangeStr );

        String v = System.getProperty( "maven.version" );
        if ( v != null )
        {
            this.version = new DefaultArtifactVersion( v );
            if ( !versionRange.containsVersion( this.version ) )
            {
                skip = true;
            }
        }
        else
        {
            out.print( "WARNING: " + getITName() + ": version range '" + versionRange
                + "' supplied but no maven version - not skipping test." );
        }
    }

    protected void runTest()
        throws Throwable
    {
        out.print( getITName() + "(" + getName() + ").." );

        if ( skip )
        {
            out.println( " Skipping (version " + version + " not in range " + versionRange + ")" );
            return;
        }

        if ( "true".equals( System.getProperty( "useEmptyLocalRepository", "false" ) ) )
        {
            setupLocalRepo();
        }

        try
        {
            super.runTest();
            out.println( " Ok" );
        }
        catch ( Throwable t )
        {
            out.println( " Failure" );
            throw t;
        }
    }

    private String getITName()
    {
        String simpleName = getClass().getName();
        int idx = simpleName.lastIndexOf( '.' );
        simpleName = idx >= 0 ? simpleName.substring( idx + 1 ) : simpleName;
        simpleName = simpleName.startsWith( "MavenIT" ) ? simpleName.substring( "MavenIT".length() ) : simpleName;
        simpleName = simpleName.endsWith( "Test" ) ? simpleName.substring( 0, simpleName.length() - 4 ) : simpleName;
        return simpleName;
    }

    protected File setupLocalRepo()
        throws IOException
    {
        String tempDirPath = System.getProperty( "maven.test.tmpdir", System.getProperty( "java.io.tmpdir" ) );
        File localRepo = new File( tempDirPath, "local-repository/" + getITName() );
        if ( localRepo.isDirectory() )
        {
            FileUtils.deleteDirectory( localRepo );
        }

        System.setProperty( "maven.repo.local", localRepo.getAbsolutePath() );

        return localRepo;
    }
}
