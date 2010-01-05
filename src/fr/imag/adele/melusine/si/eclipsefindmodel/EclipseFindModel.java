/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.imag.adele.melusine.si.eclipsefindmodel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import fr.imag.adele.melusine.as.findmodel.CheckModel;
import fr.imag.adele.melusine.as.findmodel.IFindModel;
import fr.imag.adele.melusine.as.findmodel.ModelEntry;

/**
 * @generated
 */
public class EclipseFindModel implements IFindModel {

	public static final String	MODEL_PREFIX	= "Model.";

	// private static final String NAME_SPACE = null;
	// private static final String MODEL_DEFINITION = null;

	static class BundleModelEntry implements ModelEntry {
		private Bundle	bundle;
		private String	domain;
		private String	name;
		private String	rootpath	= "";

		public BundleModelEntry(Bundle b, String domain, String name) {
			this.bundle = b;
			this.domain = domain;
			this.name = name;
			rootpath = "";
		}

		public BundleModelEntry(Bundle b, String domain, String name, String rootpath) {
			this.bundle = b;
			this.domain = domain;
			this.name = name;
			this.rootpath = rootpath;
		}

		public String getRootPath() {
			return rootpath;
		}

		public String getName() {
			return name;
		}

		public String getDomainName() {
			return domain;
		}

		public URL getURL() throws IOException {
			if (rootpath.startsWith("/")) {
				return bundle.getEntry(rootpath);
			}
			return bundle.getEntry("/" + rootpath);
		}

		public URL getEntry(String path) throws IOException {
			if ("".equals(path)) {
				return getURL();
			}
			if (rootpath.endsWith("/")) {
				return bundle.getEntry(rootpath + path);
			}
			if (path.startsWith("/")) {
				path = path.substring(1);
			}

			return bundle.getEntry(rootpath + "/" + path);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fede.v6.melusine.core.ModelEntry#list() <code>null</code> if
		 *      no entry could be found
		 */
		public ModelEntry[] list() {
			Enumeration e = bundle.getEntryPaths(rootpath);
			if (e == null) {
				return null;
			}
			List<ModelEntry> ret = new ArrayList<ModelEntry>();
			while (e.hasMoreElements()) {
				String path = (String) e.nextElement();
				ret.add(new BundleModelEntry(bundle, domain, name, path));
			}
			return ret.toArray(new ModelEntry[ret.size()]);
		}

		public ModelEntry getSubEntry(String path) {
			if (rootpath.endsWith("/")) {
				new BundleModelEntry(bundle, domain, name, rootpath + path);
			}
			return new BundleModelEntry(bundle, domain, name, rootpath + "/" + path);
		}

		public File getFile() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

	}

	public ModelEntry findQualifiedModel(String domainName, String qualifiedModelName) {
		Bundle bundle = findBundleModelEqual(qualifiedModelName);
		if (bundle == null) {
			return null;
		}
		return new BundleModelEntry(bundle, domainName, bundle.getSymbolicName());
	}

	public ModelEntry[] findModelEntries(String domainName) {
		Bundle[] bundles = findBundleModels(MODEL_PREFIX + domainName + ".");
		ModelEntry[] models = new ModelEntry[bundles.length];
		for (int i = 0; i < models.length; i++) {
			models[i] = new BundleModelEntry(bundles[i], domainName, bundles[i].getSymbolicName());
		}
		return models;
	}
	
	public ModelEntry[] findModelEntries(String domainName, CheckModel check) {
		BundleContext cxt = InternalPlatform.getDefault().getBundleContext();
		Bundle[] b = cxt.getBundles();
		List<BundleModelEntry> ret = new ArrayList<BundleModelEntry>();
		for (Bundle bundle : b) {
			BundleModelEntry e = new BundleModelEntry(bundle, domainName, bundle.getSymbolicName());
			if (check.check(e)) {
				ret.add(e);
			}
		}
		return ret.toArray(new BundleModelEntry[ret.size()]);
	}

	public ModelEntry findModelEntry(String domainName) {
		Bundle bundle = findBundleModel(MODEL_PREFIX + domainName + ".");
		if (bundle == null) {
			return null;
		}
		return new BundleModelEntry(bundle, domainName, bundle.getSymbolicName());
	}

	/**
	 * find the list of the model for the domain domainName.
	 * 
	 * @param domainName
	 *            the name of the domain
	 * @return the list
	 * @exception IllegalArgumentException(domainName+"
	 *                Model not found")
	 */
	private Bundle[] findBundleModels(String prefix) {
		BundleContext cxt = InternalPlatform.getDefault().getBundleContext();
		Bundle[] b = cxt.getBundles();
		List<Bundle> ret = new ArrayList<Bundle>();
		for (Bundle bundle : b) {
			if (bundle.getSymbolicName().startsWith(prefix)) {
				ret.add(bundle);
			}
		}
		return ret.toArray(new Bundle[ret.size()]);
	}

	private Bundle findBundleModel(String prefix) {
		BundleContext cxt = InternalPlatform.getDefault().getBundleContext();
		Bundle[] b = cxt.getBundles();
		for (Bundle bundle : b) {
			if (bundle.getSymbolicName().startsWith(prefix)) {
				return bundle;
			}
		}
		return null;
	}

	private Bundle findBundleModelEqual(String prefix) {
		BundleContext cxt = InternalPlatform.getDefault().getBundleContext();
		Bundle[] b = cxt.getBundles();
		for (Bundle bundle : b) {
			if (bundle.getSymbolicName().equals(prefix)) {
				return bundle;
			}
		}
		return null;
	}

}
