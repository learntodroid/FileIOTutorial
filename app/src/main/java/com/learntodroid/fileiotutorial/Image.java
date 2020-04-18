package com.learntodroid.fileiotutorial;

import android.net.Uri;

public class Image {
    Uri uri;
    String name;
    int size;

    public Image(Uri uri, String name, int size) {
        this.uri = uri;
        this.name = name;
        this.size = size;
    }
}
