package com.dearyoti.doahindu.utils;

public class Constant {

    public static String DB_NAME = "doahindu1.sqlite";

    // Table Category in the database.
    public static final String TBL_CATEGORY = "tbl_category";
    public static final String TBL_LATEST = "tbl_latest_story";
    public static final String TBL_CATEGORY_COLUMN_ID = "cat_id";
    public static final String TBL_CATEGORY_COLUMN_NAME = "cat_name";
    public static final String TBL_CATEGORY_COLUMN_IMAGE = "cat_image";

    // Table Topics in the database.
    public static final String TBL_TOPICS = "tbl_topics";

    public static final String TBL_TOPIC_COLUMN_ID = "topic_id";
    public static final String TBL_TOPIC_CAT_COLUMN_ID = "cat_id";
    public static final String TBL_TOPIC_COLUMN_IMAGE = "topic_image";
    public static final String TBL_TOPIC_COLUMN_NAME = "topic_name";
    public static final String TBL_TOPIC_COLUMN_STORIES = "topic_stories";
    public static final String TBL_TOPIC_COLUMN_ISFAVORITE = "topic_stories_isfav";
    public static final String TBL_TOPIC_COLUMN_LASTVIEWED = "topic_last_viewed";
    public static final String PLACEHOLDER_CONTENT =
            "Masih dalam pengerjaan, tunggu update berikutnya";

    public static final String FONT_PATH_SEMIBOLD = "fonts/montserrat_semibold.ttf";
    public static final String FONT_PATH_REGULAR = "fonts/montserrat_regular.ttf";
}
