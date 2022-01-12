package server;

import javax.sound.sampled.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    ArrayList<DataOutputStream> listeners;
    ServerSocket serverSocket;
    Socket listener;
    DataOutputStream dos;
    private final int BUFFER_SIZE = 128000;
    private File soundFile;
    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private SourceDataLine sourceLine;

    Server() {
        listeners = new ArrayList<>();
    }

    private void start() {
        try {
            serverSocket = new ServerSocket(10001);
            System.out.println("Server Started");
            new broadCast().start();

            while (true) {
                listener = serverSocket.accept();
                dos = new DataOutputStream(listener.getOutputStream());
                listeners.add(dos);
                System.out.println("Connected from [" + listener.getPort() + " : " + listener.getInetAddress() + "]");
                System.out.println("Current listener : " + listeners.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//start()

    public static void main(String[] args) {
        new Server().start();
    }//main()

    class broadCast extends Thread {
        AudioFormat format = new AudioFormat(192000.0f, 16, 2, true, false);
        TargetDataLine microphone;
        DataOutputStream lstn;

        @Override
        public void run() {
            String strFilename = "";

            try {
                soundFile = new File(strFilename);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            try {
                audioStream = AudioSystem.getAudioInputStream(soundFile);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            audioFormat = audioStream.getFormat();

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            while (true) {
                try {
                    int nBytesRead = 0;
                    byte[] abData = new byte[BUFFER_SIZE];
                    while (nBytesRead != -1) {
                        try {
                            nBytesRead = audioStream.read(abData, 0, abData.length);

                            if (nBytesRead >= 0) {
                                int size = listeners.size();
                                for (int i = 0; i < size; i++) {
                                    lstn = listeners.get(i);
                                    lstn.write(abData, 0, nBytesRead);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } catch (Throwable throwable) {
                    try {
                        lstn.close();
                        listeners.remove(lstn);
                        System.out.println("Someone Disconnected");
                        System.out.println("Current listener : " + listeners.size());
                    } catch (IOException f) {
                        f.printStackTrace();
                    }// server.Server class
                }
            }
        }
    }
}