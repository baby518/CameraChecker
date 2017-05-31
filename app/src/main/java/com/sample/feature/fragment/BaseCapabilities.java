package com.sample.feature.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BaseCapabilities {
    public static String SUPPORTED = "supported";
    public static String UNSUPPORTED = "unsupported";
    public static final String UNKNOWN = "unknown";
    public static final String NA = "N/A";
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

        public CapabilitiesItem(String title, boolean enable) {
            this.title = title;
            this.content = enable ? SUPPORTED : UNSUPPORTED;
        }

        public CapabilitiesItem(String title, Object[] contentArray) {
            this.title = title;
            if (contentArray != null && contentArray.length > 0) {
                this.content = contentArray[0].toString();
            }
        }

        public CapabilitiesItem(String title, List<?> contentList) {
            this.contentList = contentList;
            this.title = title;
            if (this.contentList != null && this.contentList.size() > 0) {
                this.content = this.contentList.get(0).toString();
            }
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
