#!groovy

import jenkins.model.Jenkins;

pluginManager = Jenkins.instance.pluginManager
updateCenter = Jenkins.instance.updateCenter

pluginManager.doCheckUpdatesServer()

["workflow-aggregator", "docker-plugin", "pipeline-stage-view"].each {
	if (! pluginManager.getPlugin(it)) {
		deployment = updateCenter.getPlugin(it).deploy(true)
		deployment.get()
	}
}

