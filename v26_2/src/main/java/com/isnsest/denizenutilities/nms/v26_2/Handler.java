package com.isnsest.denizenutilities.nms.v26_2;

import com.isnsest.denizenutilities.nms.NMSHandler;
import com.isnsest.denizenutilities.nms.v1_21.helpers.BiomeHelperImpl;
import com.isnsest.denizenutilities.nms.v26_2.helpers.FixedEntityHelperImpl;

public class Handler extends NMSHandler {

    public Handler() {
        entityHelper = new FixedEntityHelperImpl();
        biomeHelper = new BiomeHelperImpl();
    }

    @Override
    public void patchEntityHelper() {
        FixedEntityHelperImpl.patch();
    }
}
