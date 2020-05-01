package com.solace.qk;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * QooKeeper service manager configuration object. This is serialized for event transmission by org.yaml.snakeyaml.
 */
public class QKConfig {

    public QKConfig() { }
    public QKConfig(String groupName, int hashCount, int queueCount, String queuePrefix,
                    String serviceQueue, String serviceTopic, String serviceStatusTopic,
                    String topicDefinition, String objectClassName, String objectIdFieldGetter) {
        this.groupName = groupName;
        this.hashCount = hashCount;
        this.queueCount = queueCount;
        this.queuePrefix = queuePrefix;
        this.serviceQueue = serviceQueue;
        this.serviceTopic = serviceTopic;
        this.serviceStatusTopic = serviceStatusTopic;
        this.topicDefinition = topicDefinition;
        this.objectClassName = objectClassName;
        this.objectIdFieldGetter = objectIdFieldGetter;
    }

    static public QKConfig fromFile(String filename) {
        File file = new File(filename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Yaml yaml = new Yaml();
            QKConfig config = yaml.load(fis);
            return config;
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getGroupName() { return groupName; }
    public int getHashCount() {
        return hashCount;
    }
    public int getQueueCount() {
        return queueCount;
    }
    public String getQueuePrefix() {
        return queuePrefix;
    }
    public String getServiceQueue() { return serviceQueue; }
    public String getServiceStatusTopic() { return serviceStatusTopic; }
    public String getTopicDefinition() { return topicDefinition; }
    public String getObjectClassName() { return objectClassName; }
    public String getObjectIdFieldGetter() { return objectIdFieldGetter; }
    public String getServiceTopic() { return serviceTopic; }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public void setHashCount(int hashCount) { this.hashCount = hashCount; }
    public void setQueueCount(int queueCount) { this.queueCount = queueCount; }
    public void setQueuePrefix(String queuePrefix) { this.queuePrefix = queuePrefix; }
    public void setServiceQueue(String serviceQueue) { this.serviceQueue = serviceQueue; }
    public void setServiceStatusTopic(String serviceStatusTopic) { this.serviceStatusTopic = serviceStatusTopic; }
    public void setTopicDefinition(String topicDefinition) { this.topicDefinition = topicDefinition; }
    public void setObjectClassName(String objectClassName) { this.objectClassName = objectClassName; }
    public void setObjectIdFieldGetter(String objectIdFieldGetter) { this.objectIdFieldGetter = objectIdFieldGetter; }
    public void setServiceTopic(String serviceTopic) { this.serviceTopic = serviceTopic; }

    @Override
    public String toString() {
        return "QKConfig{" +
                "groupName='" + groupName + '\'' +
                ", hashCount=" + hashCount +
                ", queueCount=" + queueCount +
                ", queuePrefix='" + queuePrefix + '\'' +
                ", serviceQueue='" + serviceQueue + '\'' +
                ", serviceTopic='" + serviceTopic + '\'' +
                ", serviceStatusTopic='" + serviceStatusTopic + '\'' +
                ", topicDefinition='" + topicDefinition + '\'' +
                ", objectClassName='" + objectClassName + '\'' +
                ", objectIdFieldGetter='" + objectIdFieldGetter + '\'' +
                '}';
    }

    private String groupName;
    private int hashCount;
    private int queueCount;
    private String queuePrefix;
    private String serviceQueue;
    private String serviceTopic;
    private String serviceStatusTopic;
    private String topicDefinition;
    private String objectClassName;
    private String objectIdFieldGetter;
}
