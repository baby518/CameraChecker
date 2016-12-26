package com.sample.camerafeature.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BaseCapabilities {
    public static String SUPPORTED = "supported";
    public static String UNSUPPORTED = "unsupported";
    public static final String UNKNOWN = "unknown";
    private List<CapabilitiesItem> ITEMS = new ArrayList<>();

    public void addItem(CapabilitiesItem item) {
        this.ITEMS.add(item);
    }

    public void clearCameraCapabilities() {
        this.ITEMS.clear();
    }

    protected abstract void generateCapabilities();

    public CapabilitiesItem getItemAt(int index) {
        return this.ITEMS.get(index);
    }

    public List<CapabilitiesItem> getItems() {
        return this.ITEMS;
    }

    public class CapabilitiesItem {
        private String content;
        private List<?> contentList;
        private String title;

        public CapabilitiesItem(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public CapabilitiesItem(List<?> contentList) {
            this.contentList = contentList;
        }

        public CapabilitiesItem(Set<?> contentSet) {
        }

        public CapabilitiesItem(String title, boolean paramBoolean) {
            this.title = title;
        }

        public CapabilitiesItem(String title, Object[] paramArrayOfObject) {
        }

        public String getContent() {
            return content;
        }

        public List<?> getContentList() {
            return contentList;
        }

        public String getTitle() {
            return title;
        }

        public String toString() {
            return title;
        }
    }
}
