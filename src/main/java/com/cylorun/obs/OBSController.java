package com.cylorun.obs;

import com.cylorun.TourneyMasterOptions;
import io.obswebsocket.community.client.OBSRemoteController;

public class OBSController {

    private OBSRemoteController controller;
    private static OBSController instance;

    private OBSController() {
        TourneyMasterOptions options = TourneyMasterOptions.getInstance();
        OBSRemoteController controller = OBSRemoteController.builder()
                .host("localhost")
                .port(options.port)
                .password(options.password)
                .connectionTimeout(3)
                .build();
        controller.connect();

        this.controller = controller;
    }
    public static OBSController getInstance() {
        if (instance == null) {
            instance = new OBSController();
        }
        return instance;
    }

    public void openScene(String name) {
//        this.controller.getSceneCollectionList()
    }
}
