package com.cylorun.obs;

import com.cylorun.TourneyMasterOptions;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.request.config.GetSceneCollectionListRequest;
import io.obswebsocket.community.client.message.response.config.GetSceneCollectionListResponse;
import io.obswebsocket.community.client.message.response.scenes.GetCurrentProgramSceneResponse;
import io.obswebsocket.community.client.message.response.scenes.GetSceneListResponse;
import io.obswebsocket.community.client.model.Scene;

import java.util.List;
import java.util.function.Consumer;

public class OBSController {

    private OBSRemoteController controller;
    private boolean isConnected = false;
    private Consumer<Boolean> connectStatusConsumer;
    private static OBSController instance;

    private OBSController() {
        TourneyMasterOptions options = TourneyMasterOptions.getInstance();
        this.connect(options.obs_host, options.obs_port, options.obs_password);
    }

    private void setIsConnected(boolean b) {
        this.isConnected = b;
        System.out.println("Connected: " + b);
        if (this.connectStatusConsumer != null) {
            this.connectStatusConsumer.accept(b);
        }
    }

    public static OBSController getInstance() {
        if (instance == null) {
            instance = new OBSController();
        }
        return instance;
    }

    public void connect(int port, String password) {
        this.connect("localhost", port, password);
    }

    public void connect(String host, int port, String password) {
        this.disconnect();
        OBSRemoteController controller = OBSRemoteController.builder()
                .host(host)
                .port(port)
                .password(password)
                .build();

        controller.connect();
        this.setIsConnected(true);

        this.controller = controller;
    }

    public void onConnectStatusChanged(Consumer<Boolean> status) {
        this.connectStatusConsumer = status;
    }

    public void disconnect() {
        this.setIsConnected(false);
        if (this.controller != null) {
            this.controller.disconnect();
        }
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void openScene(String name) {
        this.controller.setCurrentProgramScene(name, (response) -> {
            if (response != null && response.isSuccessful()) {
                System.out.println("Scene switched to: " + name);
            } else {
                System.err.println("Failed to switch scene: " +
                        (response != null ? response.getMessageData().getRequestStatus().getComment() : "Unknown error"));
            }
        });
    }

    public void getSceneList(Consumer<GetSceneListResponse> consumer) {
        this.controller.getSceneList(consumer);
    }

    public void getCurrentScene(Consumer<GetCurrentProgramSceneResponse> consumer) {
        this.controller.getCurrentProgramScene(consumer);
    }

}
