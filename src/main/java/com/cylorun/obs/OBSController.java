package com.cylorun.obs;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;
import io.obswebsocket.community.client.OBSCommunicator;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.response.scenes.GetCurrentProgramSceneResponse;
import io.obswebsocket.community.client.message.response.scenes.GetSceneListResponse;

import java.util.function.Consumer;
import java.util.logging.Level;

public class OBSController {

    private OBSRemoteController controller;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private Consumer<Boolean> connectStatusConsumer;
    private static OBSController instance;

    private OBSController() {
        TourneyMasterOptions options = TourneyMasterOptions.getInstance();
        this.connect(options.obs_host, options.obs_port, options.obs_password);
    }

    private void setIsConnected(boolean b) {
        this.isConnected = b;
        if (this.connectStatusConsumer != null) {
            this.connectStatusConsumer.accept(b);
        }
    }

    public synchronized static OBSController getInstance() {
        if (instance == null) {
            instance = new OBSController();
        }
        return instance;
    }

    public void connect(int port, String password) {
        this.connect("localhost", port, password);
    }

    public synchronized void connect(String host, int port, String password) {
        if (this.isConnecting) return;

        this.isConnecting = true;
        this.disconnect();

        try {
            final OBSRemoteController[] tempControllerHolder = new OBSRemoteController[1];

            OBSRemoteController tempController = OBSRemoteController.builder()
                    .host(host)
                    .port(port)
                    .password(password)
                    .connectionTimeout(4)
                    .lifecycle()
                    .onReady(() -> {
                        TourneyMaster.log(Level.INFO, "Connected to OBS");
                        this.controller = tempControllerHolder[0];
                        this.isConnecting = false;
                        this.setIsConnected(true);
                    })
                    .onDisconnect(() -> {
                        TourneyMaster.log(Level.INFO, "Disconnected OBS controller");
                        this.isConnecting = false;
                        this.setIsConnected(false);
                    })
                    .onCommunicatorError((err) -> {
                        TourneyMaster.log(Level.SEVERE, "OBS Communication error: " + err.getReason());
                        TourneyMaster.showError("OBS Communication error, make sure OBS is running and has websocket server installed and enabled: " + err.getReason());
                        this.isConnecting = false;

//                        TourneyMaster.stop();
                    })
                    .and()
                    .build();

            tempControllerHolder[0] = tempController;

            tempController.connect();
        } catch (Exception e) {
            TourneyMaster.log(Level.SEVERE, "Exception during connection: " + e.getMessage());
            this.isConnecting = false;
        }
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
        if (this.controller == null) {
            consumer.accept(null);
            return;
        }
        this.controller.getSceneList(consumer);
    }

    public void getCurrentScene(Consumer<GetCurrentProgramSceneResponse> consumer) {
        this.controller.getCurrentProgramScene(consumer);
    }

}
