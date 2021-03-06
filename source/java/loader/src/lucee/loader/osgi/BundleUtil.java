/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.loader.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.util.ClassUtil;

import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

public class BundleUtil {
	/*public static Bundle addBundlex(BundleContext context,File bundle, boolean start) throws IOException, BundleException {
			return addBundle(context,bundle.getAbsolutePath(),bundle,start);
		}*/


	public static Bundle addBundle(CFMLEngineFactory factory,
			BundleContext context, File bundle, Log log) throws IOException,
			BundleException {
		
		return addBundle(factory, context, bundle.getAbsolutePath(), new FileInputStream(bundle), true, log);
	}
	
	public static Bundle addBundle(CFMLEngineFactory factory,
			BundleContext context, Resource bundle, Log log) throws IOException,
			BundleException {
		return addBundle(factory, context, bundle.getAbsolutePath(), bundle.getInputStream(), true, log);
	}
	
	public static Bundle addBundle(CFMLEngineFactory factory,
			BundleContext context, String path, InputStream is, boolean closeIS, Log log) throws IOException,
			BundleException {
		
		// if possible use that feature from core, it is smarter (can also load relations)
		ClassUtil cu=null;
		try{
			cu = CFMLEngineFactory.getInstance().getClassUtil();
		}
		catch(Throwable t){}
		if(cu!=null) {
			return cu.addBundle(context, is, closeIS,true);
		}
		
		
		if(log!=null)log.info("OSGI", "add bundle:" + path);
		else {
			//factory.log(Log.LEVEL_INFO, "add_bundle:" + bundle);
		}
		try {
			return installBundle(context,path, is);
		}
		finally {
			if(closeIS)CFMLEngineFactory.closeEL(is);
		}
	}
	
	public static Bundle installBundle(BundleContext context, String path, InputStream is) throws BundleException {
		return context.installBundle(path, is);
	}

	public static void start(CFMLEngineFactory factory, List<Bundle> bundles) throws BundleException {
		if (bundles == null || bundles.isEmpty())
			return;

		Iterator<Bundle> it = bundles.iterator();
		while (it.hasNext()) {
			start(factory, it.next());
		}
	}

	public static void start(CFMLEngineFactory factory, Bundle bundle) throws BundleException {
		ClassUtil cu=null;
		try{
			cu = CFMLEngineFactory.getInstance().getClassUtil();
		}
		catch(Throwable t){}
		if(cu!=null) {
			cu.start(bundle);
			return;
		}
		
		String fh = bundle.getHeaders().get("Fragment-Host");
		if (!Util.isEmpty(fh)) {
			factory.log(Logger.LOG_INFO,
					"do not start [" + bundle.getSymbolicName()
							+ "], because this is a fragment bundle for [" + fh
							+ "]");
			return;
		}

		factory.log(Logger.LOG_INFO, "start bundle:" + bundle.getSymbolicName()
				+ ":" + bundle.getVersion().toString());
		
		start(bundle);
	}
	


	public static void start(Bundle bundle) throws BundleException {
		bundle.start();
	}



	public static void startIfNecessary(CFMLEngineFactory factory, Bundle bundle) throws BundleException {
		if(bundle.getState()==Bundle.ACTIVE) return;
		start(factory, bundle);
	}

	public static String bundleState(int state, String defaultValue) {
		switch (state) {
		case Bundle.UNINSTALLED:
			return "UNINSTALLED";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED";
		case Bundle.STARTING:
			return "STARTING";
		case Bundle.STOPPING:
			return "STOPPING";
		case Bundle.ACTIVE:
			return "ACTIVE";
		}

		return defaultValue;
	}

	public static String toFrameworkBundleParent(String str) throws BundleException {
		if (str != null) {
			str = str.trim();
			if (Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK.equalsIgnoreCase(str))
				return Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK;
			if (Constants.FRAMEWORK_BUNDLE_PARENT_APP.equalsIgnoreCase(str))
				return Constants.FRAMEWORK_BUNDLE_PARENT_APP;
			if (Constants.FRAMEWORK_BUNDLE_PARENT_BOOT.equalsIgnoreCase(str))
				return Constants.FRAMEWORK_BUNDLE_PARENT_BOOT;
			if (Constants.FRAMEWORK_BUNDLE_PARENT_EXT.equalsIgnoreCase(str))
				return Constants.FRAMEWORK_BUNDLE_PARENT_EXT;
		}
		throw new BundleException("value [" + str + "] for ["
				+ Constants.FRAMEWORK_BUNDLE_PARENT
				+ "] defintion is invalid, " + "valid values are ["
				+ Constants.FRAMEWORK_BUNDLE_PARENT_APP + ", "
				+ Constants.FRAMEWORK_BUNDLE_PARENT_BOOT + ", "
				+ Constants.FRAMEWORK_BUNDLE_PARENT_EXT + ", "
				+ Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK + "]");
	}

	public static boolean isSystemBundle(Bundle bundle) {
		// TODO make a better implementation for this, independent of felix
		return bundle.getSymbolicName().equals("org.apache.felix.framework");
	}
}