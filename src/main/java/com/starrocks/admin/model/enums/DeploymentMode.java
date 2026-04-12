package com.starrocks.admin.model.enums;

public enum DeploymentMode {
    SHARED_NOTHING,
    SHARED_DATA;

    public boolean isSharedData() {
        return this == SHARED_DATA;
    }

    public boolean isSharedNothing() {
        return this == SHARED_NOTHING;
    }
}
