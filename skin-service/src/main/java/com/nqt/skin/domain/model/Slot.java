package com.nqt.skin.domain.model;

public enum Slot {
    BASE(0),
    NECK(1),
    EYES(2),
    HEAD(3),
    ACCESSORY(4);

    private final int defaultLayerOrder;

    Slot(int defaultLayerOrder) {
        this.defaultLayerOrder = defaultLayerOrder;
    }

    public int getDefaultLayerOrder() {
        return defaultLayerOrder;
    }
}
