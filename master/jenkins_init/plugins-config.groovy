#!groovy

import jenkins.model.Jenkins
import com.nirima.jenkins.plugins.docker.DockerCloud
import com.nirima.jenkins.plugins.docker.DockerTemplate
import com.nirima.jenkins.plugins.docker.DockerTemplateBase
import com.nirima.jenkins.plugins.docker.launcher.AttachedDockerComputerLauncher
import io.jenkins.docker.connector.DockerComputerAttachConnector
import com.nirima.jenkins.plugins.docker.DockerDisabled
import com.nirima.jenkins.plugins.docker.DockerImagePullStrategy
import hudson.slaves.Cloud
import io.jenkins.docker.client.DockerAPI
import io.jenkins.docker.connector.DockerComputerConnector
import org.jenkinsci.plugins.docker.commons.credentials.DockerServerEndpoint

def templateBaseParameters = [
  image:                       'jenkins/agent:latest',
  mountsString:                'type=bind,source=/var/run/docker.sock,destination=/var/run/docker.sock',
]

def templateParameters = [
  labelString:                 'maven',
  name:                        'maven',
  pullTimeout:                 300,
  remoteFs:                    '/home/jenkins/agent',
  removeVolumes:               false,
]

 def cloudParameters = [
  serverUrl:                   'tcp://192.168.50.200:2375',
  name:                        'docker',
  containerCap:                4, 
  connectTimeout:              5,
  readTimeout:                 60,
]

DockerTemplateBase templateBase = new DockerTemplateBase(templateBaseParameters.image)
templateBaseParameters.findAll{ it.key != "image" }.each { k, v ->
  templateBase."$k" = v
}

DockerComputerConnector computerConnector = new DockerComputerAttachConnector()
Set<String> templateParametersHandledSpecially = [ 'labelString', 'instanceCapStr' ]
DockerTemplate template = new DockerTemplate(
  templateBase,
  computerConnector,
  templateParameters.labelString,
  templateParameters.instanceCapStr
)

templateParameters.findAll{ !templateParametersHandledSpecially.contains(it.key) }.each { k, v ->
  if ( k=="disabled" ) {
    DockerDisabled dd = new DockerDisabled()
    dd.disabledByChoice = v
    template."$k" = dd
  } else {
    template."$k" = v
  }
}

Set<String> cloudParametersHandledSpecially = [ 'serverUrl', 'credentialsId' ,'serverUrl' ,'credentialsId' ,'connectTimeout' ,'readTimeout' ,'version' ,'connectTimeout' ,'dockerHostname' ,'name' ]
DockerAPI api = new DockerAPI(new DockerServerEndpoint(cloudParameters.serverUrl, cloudParameters.credentialsId))
api.with {
  connectTimeout = cloudParameters.connectTimeout
  readTimeout = cloudParameters.readTimeout
  apiVersion = cloudParameters.version
  hostname = cloudParameters.dockerHostname
}
DockerCloud newCloud = new DockerCloud(
  cloudParameters.name,
  api,
  [template]
)
cloudParameters.findAll{!cloudParametersHandledSpecially.contains(it.key)}.each { k, v ->
  if ( k=="disabled" ) {
    DockerDisabled dd = new DockerDisabled()
    dd.disabledByChoice = v
    newCloud."$k" = dd
  } else {
    newCloud."$k" = v
  }
}

/////////////////////////////////////////////////////////////////////////////
// Now to push our data into Jenkins,
// replacing (overwriting) any cloud of the same name with this config.
/////////////////////////////////////////////////////////////////////////////

// get Jenkins instance
Jenkins jenkins = Jenkins.get()

// add/replace cloud configuration to Jenkins
Cloud oldCloudOrNull = jenkins.clouds.getByName(cloudParameters.name)
if ( oldCloudOrNull ) {
  jenkins.clouds.remove(oldCloudOrNull)
}
jenkins.clouds.add(newCloud)

// save current Jenkins state to disk
jenkins.save()

/////////////////////////////////////////////////////////////////////////////
// ...and we're done.
////////////////////////////////////////////////////////////////////////////