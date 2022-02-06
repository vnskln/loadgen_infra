#!groovy

import jenkins.model.Jenkins;

pluginManager = Jenkins.instance.pluginManager
updateCenter = Jenkins.instance.updateCenter

pluginManager.doCheckUpdatesServer()

["workflow-aggregator", "docker-plugin"].each {
	if (! pluginManager.getPlugin(it)) {
		deployment = updateCenter.getPlugin(it).deploy(true)
		deployment.get()
	}
}

