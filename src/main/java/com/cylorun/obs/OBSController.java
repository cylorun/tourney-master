package com.cylorun.obs;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.request.sceneitems.SetSceneItemTransformRequest;
import io.obswebsocket.community.client.message.response.scenes.GetCurrentProgramSceneResponse;
import io.obswebsocket.community.client.message.response.scenes.GetSceneListResponse;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;

public class OBSController {

    private boolean isConnected = false;
    private boolean isConnecting = false;
    private static OBSController instance;

    private OBSController() {
        TourneyMasterOptions options = TourneyMasterOptions.getInstance();
    }


    public static synchronized OBSController getInstance() {
        if (instance == null) {
            instance = new OBSController();
        }
        return instance;
    }

    public void openScene(String name) {

    }

    public void getSceneList(Consumer<GetSceneListResponse> consumer) {
        if (this.controller == null) {
            consumer.accept(null);
            return;
        }

        this.controller.getSceneList(consumer);
    }
}
