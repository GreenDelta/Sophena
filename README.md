Sophena
=======
Sophena is a tool for calculating heating networks...

## Development tools
* Eclipse for RCP/RAP developers (Mars)
* e(fx)clipse (install via update site)
* JDK > 8.x installed

### Target platform
Build the target platform via the ANT script in the [runtime](./runtime)
folder.

**TODO** it is currently required to put manually the following e(fx)clipse
plugins into the target platform:

* org.eclipse.fx.osgi
* org.eclipse.fx.ui.workbench3

Make sure that these plugins are added to the plugin dependencies and the
dependencies of the product configuration. Additionally, the following
VM argument must be added to the launching configuration:

    -Dosgi.framework.extensions=org.eclipse.fx.osgi



