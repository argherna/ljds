package com.github.argherna.javadocserver;

import static java.util.Objects.isNull;

import java.util.Collections;
import java.util.List;

class ContentTypeEntry {

    private final String mimetype;

    private final String description;

    private final List<String> extensions;

    private final String icon;

    private final Action action;

    private final String application;

    static enum Action {
        BROWSER, APPLICATION, SAVE, UNKNOWN;
    }

    ContentTypeEntry(final String mimetype, final String description,
            final List<String> extensions, final String icon, final Action action,
            final String application) {
        this.mimetype = mimetype;
        this.description = description;
        this.extensions = extensions;
        this.icon = icon;
        this.action = action;
        this.application = application;
    }

    String getMimetype() {
        return mimetype;
    }

    String getDescription() {
        return description;
    }

    List<String> getExtensions() {
        return isNull(extensions) ? List.of() : Collections.unmodifiableList(extensions);
    }

    String getIcon() {
        return icon;
    }

    Action getAction() {
        return action;
    }

    String getApplication() {
        return application;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((application == null) ? 0 : application.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
        result = prime * result + ((icon == null) ? 0 : icon.hashCode());
        result = prime * result + ((mimetype == null) ? 0 : mimetype.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContentTypeEntry other = (ContentTypeEntry) obj;
        if (action != other.action)
            return false;
        if (application == null) {
            if (other.application != null)
                return false;
        } else if (!application.equals(other.application))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (extensions == null) {
            if (other.extensions != null)
                return false;
        } else if (!extensions.equals(other.extensions))
            return false;
        if (icon == null) {
            if (other.icon != null)
                return false;
        } else if (!icon.equals(other.icon))
            return false;
        if (mimetype == null) {
            if (other.mimetype != null)
                return false;
        } else if (!mimetype.equals(other.mimetype))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ContentTypeEntry [action=" + action + ", application=" + application
                + ", description=" + description + ", extensions=" + extensions.toString()
                + ", icon=" + icon + ", mimetype=" + mimetype + "]";
    }

}
