package com.nqmobile.livesdk.commons.modal;

/**
 * Item definition including the section.
 */
public class SectionListItem {
    public Object item;
    public String section;
    public int seq;

    public SectionListItem(final Object item, final String section) {
        this.item = item;
        this.section = section;
        this.seq = 0;
    }
    
    public SectionListItem(final Object item, final String section, final int seq) {
        this.item = item;
        this.section = section;
        this.seq = seq;
    }

    @Override
    public String toString() {
        return item.toString();
    }

}
