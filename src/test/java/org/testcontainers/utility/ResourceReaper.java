package org.testcontainers.utility;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import org.testcontainers.DockerClientFactory;

import java.util.List;
import java.util.Map;

/**
 *  Test containers 1.9.1 doesn't work with Bitbucket pipelines.
 *  Test containers start a docker with Ryuk. Ryuk needs to mount the docker socket, and this is forbidden in Bitbucket pipelines.
 *  Ryuk isn't a critical feature. It makes after test docker container cleanup more robust.
 *  This class is a hack to disable the feature. Classloader should pick our version because it's earlier on the classpath.
 *  Sorry for the hack, but I neither understand how to fix Ryuk problems, neither want to fork and release forked test-containers.
 *
 *  See:
 *  https://github.com/testcontainers/testcontainers-java/issues/712
 *
 *  Please remove it after the issue resolved, and keep the class updated when upgrading test-containers.
 */
@SuppressWarnings("ALL")
public final class ResourceReaper {
    private static ResourceReaper instance;

    private ResourceReaper() {
    }

    @Deprecated
    public static String start(String hostIpAddress, DockerClient client, boolean withDummyMount) {
        return start(hostIpAddress, client);
    }

    public static String start(String hostIpAddress, DockerClient client) {
        instance();
        return "id";
    }

    public synchronized static ResourceReaper instance() {
        if (instance == null) {
            instance = new ResourceReaper();
        }

        return instance;
    }

    public synchronized void performCleanup() {
    }

    public void registerFilterForCleanup(List<Map.Entry<String, String>> filter) {
    }

    public void registerContainerForCleanup(String containerId, String imageName) {
    }

    public void stopAndRemoveContainer(String containerId) {
        stopContainer(containerId);
    }

    public void stopAndRemoveContainer(String containerId, String imageName) {
        stopContainer(containerId);
    }

    public void registerNetworkIdForCleanup(String id) {
    }

    public void registerNetworkForCleanup(String networkName) {
    }

    public void removeNetworkById(String id) {
    }

    public void removeNetworks(String identifier) {
    }

    public void unregisterNetwork(String identifier) {
    }

    public void unregisterContainer(String identifier) {
    }

    private void stopContainer(String containerId) {
        DockerClient dockerClient = DockerClientFactory.instance().client();
        boolean running;
        try {
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
            running = containerInfo.getState().getRunning();
        } catch (NotFoundException e) {
            return;
        } catch (DockerException e) {
            return;
        }

        if (running) {
            try {
                dockerClient.killContainerCmd(containerId).exec();
            } catch (DockerException e) {
            }
        }

        try {
            dockerClient.inspectContainerCmd(containerId).exec();
        } catch (NotFoundException e) {
            return;
        }

        try {
            try {
                dockerClient.removeContainerCmd(containerId).withRemoveVolumes(true).withForce(true).exec();
            } catch (InternalServerErrorException e) {
            }
        } catch (DockerException e) {
        }
    }
}
