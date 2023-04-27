
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.sql.CommonDataSource;

public class Tintolmarket{

    public static void main(String[] args) throws IOException {
        Socket socket = null;

        String serverAddress = args[0];
        int port;
        String[] split = serverAddress.split(":");
        String host = split[0];
        Scanner sc = new Scanner(System.in);
        if (serverAddress.contains(":")){
            port = Integer.parseInt(split[1]);
        }
        else{
            port = 12345;
        }

        String truststore = args[1];
        String keystore = args[2];
        String passwdK = args[3];
        String clientID = args[4];

        String passwd;
        if (args.length == 6) {
            passwd = args[5];
        }else {
            System.out.println("Insira a sua password: ");
            passwd = sc.nextLine();
        }

        try {
            socket = new Socket(host,port);
        } catch (IOException e) {
            System.err.println(e.getMessage());
			System.exit(-1);
        }
        System.out.println("Ligação estabelecida");
        ObjectOutputStream outStream = null;
		ObjectInputStream inStream = null;

        try {
            outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());

            outStream.writeObject(clientID);
            outStream.writeObject(passwd);

            String resposta = (String) inStream.readObject();
            if (resposta.equals("You are in!")) {
                System.out.println(resposta);
            } else {
                System.out.println(resposta);
                System.exit(-1);
            }

            String comand = "";
            System.out.println("Comandos: ");
            System.out.println("add <wine> <image> ");
            System.out.println("sell <wine> <value> <quantity>");
            System.out.println("view <wine> ");
            System.out.println("buy <wine> <seller> <quantity> ");
            System.out.println("wallet ");
            System.out.println("classify <wine> <stars> ");
            System.out.println("talk <user> <message> ");
            System.out.println("read ");
			System.out.println("exit ");
            while(!(comand.equals("exit") || comand.equals("e"))){
                System.out.println("Insira um comando: ");
                comand = sc.nextLine();
                outStream.writeObject(comand);
                String[] cmdSpt = comand.split(" ");
                if (cmdSpt[0].equals("add") || cmdSpt[0].equals("a")) {
                    if (inStream.readObject().equals("waiting")) {
                        File img = new File(cmdSpt[2]);
                        FileInputStream fin = new FileInputStream(img);
                        InputStream input = new BufferedInputStream(fin);
                        outStream.writeObject((int) img.length());
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            outStream.write(buffer, 0, bytesRead);
                        }
                        input.close();
                    }         
                }
                System.out.println(inStream.readObject());
            }
            sc.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        outStream.close();
		inStream.close();
 			
		socket.close();
    }

}