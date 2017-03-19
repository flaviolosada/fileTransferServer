/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.furb.server;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author usuario
 */
public class Server {

    /**
     * @param args the command line arguments
     */
    
    private static String ipCliente = "";
    
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2500);
        String serverType = "[TEXT]";//args[0];
        int porta = 5000;
        String ip = "227.55.77.99";
        MulticastSocket socket = new MulticastSocket(porta);
        InetAddress endereco = InetAddress.getByName(ip);
        socket.joinGroup(endereco);
        
        while(true) {
       	      byte[] recvData = new byte[1024];
              DatagramPacket recvPacket;
	      recvPacket = new DatagramPacket (recvData,
			      recvData.length);
              socket.receive (recvPacket);
	      
              String sentence = new String(recvPacket.getData());
              if (serverType.equals(sentence.trim())) {        
                ipCliente = recvPacket.getAddress().toString().substring(1);  
                new Thread() {
                    @Override
                    public void run() {
                        sendMyName(ipCliente); 
                        receiveFile(serverSocket);
                    }                    
                }.start();
              } else {
                  ipCliente = "";
              }
	      sentence= null;
	      recvPacket = null;
        }
        
    }
    
    public static void sendMyName(String ip) {
        DatagramSocket socket = null;
        DatagramPacket request = null;
        DatagramPacket reply = null;
        int serverPort = 5005;
        byte[] buf = new byte[1024];
        
        try {
            /* Pegar parametros */
            String mensagemEnviar = "IP: " + InetAddress.getLocalHost().getHostAddress();
            /* Inicializacao de sockets UDP com Datagrama */
            socket = new DatagramSocket();
            /* Configuracao a partir dos parametros */
            InetAddress host = InetAddress.getByName(ip);
            byte[] m = mensagemEnviar.getBytes();
            /* Criacao do Pacote Datagrama para Envio */
            request = new DatagramPacket(m, m.length, host, serverPort);
            /* Envio propriamente dito */
            socket.send(request);
            /* Preparacao do Pacote Datagrama para Recepcao */
            /* Finaliza tudo */
            socket.close ();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    public static void receiveFile(ServerSocket serverSocket) {
        byte[] stream = new byte[1024];

        try {
            Socket clientSocket = serverSocket.accept();
            /* Preparacao dos fluxos de entrada e saida */
            BufferedInputStream bf = new BufferedInputStream(clientSocket.getInputStream());
            bf.read(stream);
            
            Path file = Paths.get("C:\\Temp\\arquivo.txt");
            
            FileOutputStream fos = new FileOutputStream(file.toFile());
            
            fos.write(stream);
            /* Finaliza tudo */
            bf.close();
            fos.flush();
            fos.close();
            clientSocket.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
}
