package com.julio.clpmonitor.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.julio.clpmonitor.clp.PlcConnector;
import com.julio.clpmonitor.model.ClpData;

@Service
public class ClpSimulatorService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private PlcConnector plcConnectorEstoque;
    public static byte[] indexColorEst = new byte[28];

    private PlcConnector plcConnectorExpedicao;
    public static byte[] indexColorExp = new byte[28];

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);

        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    public void sendClp1Update() {
        plcConnectorEstoque = new PlcConnector("10.74.241.10", 102);
        List<Integer> byteArray = new ArrayList<>();

        try {
            plcConnectorEstoque.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            indexColorEst = plcConnectorEstoque.readBlock(9, 68, 28);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 28; i++) {
            byteArray.add((int) indexColorEst[i]);
        }

        ClpData clp1 = new ClpData(1, byteArray);
        sendToEmitters("clp1-data", clp1);

    }

    public void sendExpeditionUpdate() {
        plcConnectorExpedicao = new PlcConnector("10.74.241.40", 102);
        List<Integer> byteArray = new ArrayList<>();
        int returns[] = new int[12];

        try {
            plcConnectorExpedicao.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int j = 0;
            for (int i = 6; i <= 28; i += 2) {
                returns[j] = plcConnectorExpedicao.readInt(9, i);
                j++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 12; i++) {
            byteArray.add(returns[i]);
        }

        ClpData expeditionData = new ClpData(4, byteArray);
        sendToEmitters("expedition-data", expeditionData);
    }

    // sendToEmitters() – Envia um evento SSE para todos os clientes
    private void sendToEmitters(String eventName, ClpData clpData) {
        // Percorre todos os SseEmitters conectados.
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(clpData));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

}