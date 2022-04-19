package controller;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import communications.CommunicationController;
import communications.ProtocolDataPacket;

public class PacketSender extends Thread{

    BlockingQueue<ProtocolDataPacket> packetsList;
    private CommunicationController controller;

    public PacketSender(CommunicationController controller) {
        this.packetsList = new LinkedBlockingDeque<>();
        this.controller = controller;
    }

    public BlockingQueue<ProtocolDataPacket> getPacketsList() {
        return packetsList;
    }

    @Override
    public void run() {
        while (true){
            try {
                controller.sendMessage(this.packetsList.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
