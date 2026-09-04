package com.dearyoti.doahindu.model;

public class CategoryModel {

    private String cat_name;
    private Integer cat_id;
    private byte[] cat_image;

    public CategoryModel(Integer cat_id, String cat_name, byte[] cat_image) {
        this.cat_id = cat_id;
        this.cat_name = cat_name;
        this.cat_image = cat_image;
    }

    public Integer getCat_id() {
        return cat_id;
    }

    public void setCat_id(Integer cat_id) {
        this.cat_id = cat_id;
    }

    public String getCat_name() {
        return cat_name;
    }

    public void setCat_name(String cat_name) {
        this.cat_name = cat_name;
    }

    public byte[] getCat_image() {
        return cat_image;
    }

    public void setCat_image(byte[] cat_image) {
        this.cat_image = cat_image;
    }
}
