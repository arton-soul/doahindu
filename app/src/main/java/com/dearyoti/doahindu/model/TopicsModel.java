package com.dearyoti.doahindu.model;

public class TopicsModel {

    private Integer topic_id, cat_id;
    private String topic_name, topic_story;
    private byte[] topic_image;
    private Boolean is_topic_fav;
    private String last_viewed;

    public TopicsModel(Integer topic_id, Integer cat_id, String topic_name, byte[] topic_image,
                       String topic_story, Boolean is_topic_fav, String last_viewed) {
        this.topic_id = topic_id;
        this.cat_id = cat_id;
        this.topic_name = topic_name;
        this.topic_image = topic_image;
        this.topic_story = topic_story;
        this.is_topic_fav = is_topic_fav;
        this.last_viewed = last_viewed;
    }

    public Integer getTopic_id() {
        return topic_id;
    }

    public void setTopic_id(Integer topic_id) {
        this.topic_id = topic_id;
    }

    public Integer getCat_id() {
        return cat_id;
    }

    public void setCat_id(Integer cat_id) {
        this.cat_id = cat_id;
    }

    public String getTopic_name() {
        return topic_name;
    }

    public void setTopic_name(String topic_name) {
        this.topic_name = topic_name;
    }

    public byte[] getTopic_image() {
        return topic_image;
    }

    public void setTopic_image(byte[] topic_image) {
        this.topic_image = topic_image;
    }

    public String getTopic_story() {
        return topic_story;
    }

    public void setTopic_story(String topic_story) {
        this.topic_story = topic_story;
    }


    public Boolean getIs_topic_fav() {
        return is_topic_fav;
    }

    public void setIs_topic_fav(Boolean is_topic_fav) {
        this.is_topic_fav = is_topic_fav;
    }

    public String getLast_viewed() {
        return last_viewed;
    }

    public void setLast_viewed(String last_viewed) {
        this.last_viewed = last_viewed;
    }
}
