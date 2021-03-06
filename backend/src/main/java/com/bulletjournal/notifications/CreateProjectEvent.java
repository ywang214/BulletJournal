package com.bulletjournal.notifications;

import com.bulletjournal.contents.ContentType;

import java.util.List;

public class CreateProjectEvent extends Informed {

    public CreateProjectEvent(Event event, String originator) {
        super(event, originator);
    }

    public CreateProjectEvent(List<Event> event, String originator) {
        super(event, originator);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.PROJECT;
    }

    @Override
    protected String getEventTitle(Event event) {
        return this.getOriginator() + " created Project " + event.getContentName();
    }
}
