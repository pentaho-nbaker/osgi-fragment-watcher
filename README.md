## Fragment Watcher
----------

### Introduction
OSGI Fragments are a useful tool for adding capabilities to legacy libraries which aren't yet using OSGI best practices. Unfortunately, Fragments are very much frowned upon in the community. If a fragment is installed before it's host, it will be attached. If the fragment is installed after the host, it's not attached. This bundle solves the second case.

Fragment Watcher will watch for any Fragment Bundle being installed. It then checks to see if the Host Bundle for that fragment is already installed and resolved in the system. If so, the host will be refreshed to allow the fragment to attach.

### Usage
Ideally the FragmentWatcher bundle should be installed and started before any fragments and hosts. If this is not possible, extending the FragmentWatcher to inspect already installed bundles is possible though not in the scope of my initial usage: http://www.osgi.org/javadoc/r4v42/org/osgi/service/packageadmin/PackageAdmin.html#getFragments(org.osgi.framework.Bundle)