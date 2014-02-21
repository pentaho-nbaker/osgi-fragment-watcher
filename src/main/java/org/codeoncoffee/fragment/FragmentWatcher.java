package org.codeoncoffee.fragment;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * This Class is both a BundleActivator as well as an EventHandler. It watches for Bundle installed events. Checks to
 * see if the newly installed bundle has a Fragment-Host. If so it tries to find the host and updates the host bundle if
 * it can be found
 * <p/>
 * The effect to is allow fragments to be installed in the BundleContext after their Host has been.
 * <p/>
 * Created by nbaker on 2/20/14.
 */
public class FragmentWatcher implements BundleActivator, EventHandler {
  private BundleContext bundleContext;
  private LogService logService;

  @Override public void start( BundleContext bundleContext ) throws Exception {
    this.bundleContext = bundleContext;
    // get the LogService
    logService = bundleContext.getService( bundleContext.getServiceReference( LogService.class ) );

    // Check for already loaded fragments that failed to attach to their hosts
    // PackageAdmin is deprecated, but I cannot find how to replicate this code with BundleWiring apis
    PackageAdmin pAdmin = bundleContext.getService( bundleContext.getServiceReference( PackageAdmin.class ) );
    final Bundle[] bundles = bundleContext.getBundles();
    for ( Bundle bundle : bundles ) {
      final String host = bundle.getHeaders().get( "Fragment-Host" );
      if ( host != null && pAdmin.getHosts( bundle ) == null ) {
        restartHost( host );
      }
    }


    // Register this instance as an EventHandler
    String[] topics = new String[] {
      "org/osgi/framework/BundleEvent/INSTALLED"
    };
    Dictionary<String, Object> props = new Hashtable<String, Object>();
    props.put( EventConstants.EVENT_TOPIC, topics );
    bundleContext.registerService( EventHandler.class.getName(), this, props );

  }

  @Override public void stop( BundleContext bundleContext ) throws Exception {

  }

  @Override public void handleEvent( Event event ) {
    final Long bundleId = (Long) event.getProperty( "bundle.id" );
    Bundle bundle = bundleContext.getBundle( bundleId );
    final String host = bundle.getHeaders().get( "Fragment-Host" );
    if ( host != null ) {
      logService.log( LogService.LOG_INFO, "Fragment detected for host: " + host );
      restartHost( host );
    }
  }

  private void restartHost( String host ) {
    try {
      final Bundle[] bundles = bundleContext.getBundles();
      for ( Bundle b : bundles ) {
        if ( host.equals( b.getSymbolicName() ) && b.getState() > Bundle.INSTALLED ) {
          b.update();
          logService.log( LogService.LOG_INFO, "Host: " + host + " reloaded to attach fragment" );
          return;
        }
      }
    } catch ( BundleException e ) {
      logService.log( LogService.LOG_ERROR, "Bundle Exception trying to find Fragment-Host to restart", e );
    } catch ( Exception e ) {
      logService.log( LogService.LOG_ERROR, "Unknown Error trying to find Fragment-Host to restart", e );
    }
  }
}