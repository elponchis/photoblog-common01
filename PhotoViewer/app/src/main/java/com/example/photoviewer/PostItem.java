package com.example.photoviewer;

import android.graphics.Bitmap;

public class PostItem {

    private final String title;
    private final String text;
    private final String publishedDate;
    private final Bitmap image;

    public PostItem(String title, String text, String publishedDate, Bitmap image) {
        this.title = title;
        this.text = text;
        this.publishedDate = publishedDate;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getPublishedDate() {
        if (publishedDate == null || publishedDate.length() < 10) {
            return "";
        }
        String date = publishedDate.substring(0, 10);
        if (publishedDate.length() >= 16) {
            return date + " " + publishedDate.substring(11, 16);
        }
        return date;
    }

    public Bitmap getImage() {
        return image;
    }
}
