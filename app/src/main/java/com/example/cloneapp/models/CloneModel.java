package com.example.cloneapp.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model representing a cloned application instance.
 */
public class CloneModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String targetPackage;
    private String customName;
    private String customIconBase64;
    private boolean isHidden;
    private long createdAt;
    /** Engine user space id this clone lives in; -1 = not installed in the engine yet. */
    private int virtualUserId = -1;

    public CloneModel() {
    }

    public CloneModel(String id, String targetPackage, String customName, String customIconBase64, boolean isHidden, long createdAt) {
        this.id = id;
        this.targetPackage = targetPackage;
        this.customName = customName;
        this.customIconBase64 = customIconBase64;
        this.isHidden = isHidden;
        this.createdAt = createdAt;
    }

    public int getVirtualUserId() {
        return virtualUserId;
    }

    public void setVirtualUserId(int virtualUserId) {
        this.virtualUserId = virtualUserId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCustomIconBase64() {
        return customIconBase64;
    }

    public void setCustomIconBase64(String customIconBase64) {
        this.customIconBase64 = customIconBase64;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloneModel that = (CloneModel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
