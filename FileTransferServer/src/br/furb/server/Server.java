/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.furb.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
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
import java.util.Properties;

/**
 *
 * @author usuario
 */
public class Server {

    /**
     * @param args the command line arguments
     */
    
    private static String clientIP = "";
    private static String fileName = "";
    private static String dirBackup = "";
    private static int serverPortNumber = 0;
    
    public static void main(String[] args) throws IOException {
        Properties prop = getProp();
        serverPortNumber = Integer.parseInt(prop.getProperty("socketPort"));
        ServerSocket serverSocket = new ServerSocket(serverPortNumber);
        String serverType = prop.getProperty("serverType");
        dirBackup = prop.getProperty("dirBackup");
        System.out.println("\\------- Configurações do servidor -------/");
        System.out.println("Tipo de servidor: " + serverType);
        System.out.println("Porta TCP.......: " + serverPortNumber);
        System.out.println("Diretório Backup: " + dirBackup);
        System.out.println("/------- Configurações do servidor -------\\");
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
	      
              String sentence = new String(recvPacket.getData()).trim();
              if (sentence.startsWith(serverType)) {        
                clientIP = recvPacket.getAddress().toString().substring(1);
                fileName = sentence.split("]")[1];
                new Thread() {
                    @Override
                    public void run() {
                        sendMyName(clientIP); 
                        receiveFile(serverSocket);
                    }                    
                }.start();
              } else {
                  clientIP = "";
                  fileName = "";
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
            String mensagemEnviar = "IP: " + InetAddress.getLocalHost().getHostAddress() + ":" + serverPortNumber;
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
            
            InputStream in = clientSocket.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            String fName = reader.readLine();
            Path file = Paths.get(dirBackup + fName);
            System.out.println("Arquivo recebido: " + fName);
            FileOutputStream out = new FileOutputStream(file.toFile());
            int tamanho = 4096; // buffer de 4KB  
            byte[] buffer = new byte[tamanho];  
            int lidos = -1;  
            while ((lidos = in.read(buffer, 0, tamanho)) != -1) {  
                out.write(buffer, 0, lidos);  
            }  
            out.flush();  
            out.close();
            clientSocket.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public static Properties getProp() throws IOException {
        Properties props = new Properties();
        FileInputStream file = new FileInputStream("./config.properties");
        props.load(file);
        return props;

    }
    
}
