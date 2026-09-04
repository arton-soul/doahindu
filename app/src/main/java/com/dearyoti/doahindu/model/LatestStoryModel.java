package com.dearyoti.doahindu.model;

public class LatestStoryModel {

    private String topic_name;
    private String topic_story;
    private Integer topic_id;
    private byte[] topic_image;

    public LatestStoryModel(Integer topic_id, String topic_name, byte[] topic_image, String topic_story) {
        this.topic_id = topic_id;
        this.topic_name = topic_name;
        this.topic_story = topic_story;
        this.topic_image = topic_image;
    }

    public String getTopic_name() {
        return topic_name;
    }

    public void setTopic_name(String topic_name) {
        this.topic_name = topic_name;
    }

    public String getTopic_story() {
        return topic_story;
    }

    public void setTopic_story(String topic_story) {
        this.topic_story = topic_story;
    }

    public Integer getTopic_id() {
        return topic_id;
    }

    public void setTopic_id(Integer topic_id) {
        this.topic_id = topic_id;
    }

    public byte[] getTopic_image() {
        return topic_image;
    }

    public void setTopic_image(byte[] topic_image) {
        this.topic_image = topic_image;
    }


}
