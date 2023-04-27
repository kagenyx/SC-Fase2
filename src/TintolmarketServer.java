import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileInputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;

//import javax.crypto.spec.SecretKeyFactory;
import javax.crypto.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;

public class TintolmarketServer{

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
		System.out.println("Servidor TintolmarketServer: main");
		TintolmarketServer server = new TintolmarketServer();
        int port = 12345;
		String passwdC = null;
        if(args.length == 3){
			passwdC = args[0];
			String keystore = args[1];
        	String passwdK = args[2];
		}
        else {
			if(args.length == 4){
				port = Integer.parseInt(args[0]);
				passwdC = args[1];
				String keystore = args[2];
				String passwdK = args[3];
			}else {
				System.out.println("Número incorreto de argumentos!");
				System.exit(-1);
			}
		}

		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
		//Generate the key based on the password

		PBEKeySpec keySpec = new PBEKeySpec(passwdC.toCharArray(), salt, 20); 
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		SecretKey key = kf.generateSecret(keySpec);

		//ENCRYPTION: Lets check that the two keys are equivalent by encrypting a string
		Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] params = c.getParameters().getEncoded(); // we need to get the various parameters (p.ex., IV)

		//DECRYPTION: Now lets see if we get the original string (NOTE: get key exactly as above)
		AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
		p.init(params);
		Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		d.init(Cipher.DECRYPT_MODE, key, p);

		server.startServer(port,c, d);
	}

	public void startServer (int port, Cipher c, Cipher d){
		ServerSocket sSoc = null;        
		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
         
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc,c,d);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		//sSoc.close();
	}


	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;
		private Cipher c;
		private Cipher d;

		ServerThread(Socket inSoc, Cipher c, Cipher d) {
			socket = inSoc;
			this.c = c;
			this.d = d;
		}
 
		public void run(){
			try {

				FileInputStream fis;
				FileOutputStream fos;
				CipherOutputStream cos;
				
				fis = new FileInputStream("clientIDs.txt");
				fos = new FileOutputStream("clientIDs.cif");

				cos = new CipherOutputStream(fos, c);
				byte[] b = new byte[16];  
				int j = fis.read(b);
				while (j != -1) {
					cos.write(b, 0, j);
					j = fis.read(b);
				}
				
				cos.close();
				fis.close();
				fos.close();

				//byte[] keyEncoded = key.getEncoded();
				FileOutputStream kos = new FileOutputStream("a.key");
				ObjectOutputStream oos = new ObjectOutputStream(kos);
				//oos.writeObject(keyEncoded);
				oos.close();
				kos.close();
				

				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String passwd = null;
				
				// user e passwd do client
				try {
					user = (String)inStream.readObject();
					passwd = (String)inStream.readObject();

				}catch (ClassNotFoundException e1) {

				}
                
				// ler e escrever no ficheiro clientIDs.txt
                File f;
                FileWriter writer;
                Scanner sc;
                String content = "";
                boolean autenticado = false;

                try {
                    f = new File("./clientIDs.txt");
                    sc = new Scanner(f);
                    String line;
                    while(sc.hasNextLine()){
                        line = sc.nextLine();
                        content = content.concat(line + "\n");
                        if (line.contains(user) && !autenticado) {
                            String[] lineSplit = line.split(":");
                            if (lineSplit[0].equals(user) && lineSplit[1].equals(passwd)) {
                                autenticado = true;
								outStream.writeObject("You are in!");
                            }
                            else {
                                System.out.println("Wrong password");
								outStream.writeObject("Wrong password");
                                System.exit(-1);
                            }
                                
                        }
                    }
                    sc.close();
                    writer = new FileWriter("./clientIDs.txt");

                } catch (Exception e) {
                    writer = new FileWriter("./clientIDs.txt");
                }

				writer.write(content);
                if (!autenticado){
                    writer.write(user + ":" + passwd + "\n");
                }
				writer.close();
				File saldoFile;
				Scanner wallets;
				String saldoContent = "";
                FileWriter saldoWr;
				boolean existe = false;
				try {
					saldoFile = new File("./wallet.txt");
					wallets = new Scanner(saldoFile);
					while(wallets.hasNextLine()){
						String line = wallets.nextLine();
						saldoContent = saldoContent.concat(line + "\n");
						String[] lineSplit = line.split("-");
						if (lineSplit[0].equals(user)) {
							existe = true;
						}
					}
					saldoWr = new FileWriter("./wallet.txt");
				} catch (Exception e) {
					saldoWr = new FileWriter("./wallet.txt");
				}
				saldoWr.write(saldoContent);
				if (!existe) {
					saldoWr.write(user + "-200");
				}
				saldoWr.close();
				// autentificação feita
				System.out.println("User " + user + " conectado");

				boolean continua = true;
				String comando;
				while (continua){
					comando = (String)inStream.readObject();
					System.out.print(user + ": ");
					System.out.println(comando);
					String[] comandoSplit = comando.split(" ");
										
					File wineList;
					FileWriter wr;
					String wineContent = "";
					Scanner sc1;
					boolean erro = false;

					if (comandoSplit[0].equals("add") || comandoSplit[0].equals("a")) {

						try {
							wineList = new File("./Wines.txt");
							sc1 = new Scanner(wineList);
							while(sc1.hasNextLine()){
								String line = sc1.nextLine();
								wineContent = wineContent.concat(line + "\n");
								String[] wineSplit = line.split("-");
								if (wineSplit[0].equals(comandoSplit[1]))
									erro = true;
							}
							sc1.close();
							wr = new FileWriter("./Wines.txt");
						} catch (Exception e) {
							wr = new FileWriter("./Wines.txt");
						}
						wr.write(wineContent);
						if(erro){
							outStream.writeObject("not waiting");
							outStream.writeObject("Erro o vinho que tentou adicionar já está registado");
						}
						else{
							outStream.writeObject("waiting");
							File img = new File(comandoSplit[2]);
							FileOutputStream fout = new FileOutputStream(img);
							OutputStream output = new BufferedOutputStream(fout);
							byte[] buffer = new byte[1024];
							int bytesRead;
							int fileSize;

							try {
								fileSize = (int) inStream.readObject();
								int totalSize = fileSize;

								while (totalSize > 0) {
									if (totalSize >= 1024) {
										bytesRead = inStream.read(buffer,0,1024);
									} else {
										bytesRead = inStream.read(buffer,0,totalSize);
									}
									output.write(buffer,0,bytesRead);
									totalSize -= bytesRead;
								}
								output.close();
								fout.close();
								
							} catch (Exception e) {
								
							}
							output.write(buffer, 0, 1024);
							//          wine                      image        stars 
							wr.write(comandoSplit[1] + "-" + comandoSplit[2] + "-0\n");
							outStream.writeObject("Vinho " + comandoSplit[1] + " adicionado");
						}
						wr.close();
					}

					if (comandoSplit[0].equals("sell") || comandoSplit[0].equals("s")) {
						erro = true;
						try {
							wineList = new File("./Wines.txt");
							sc1 = new Scanner(wineList);
							while(sc1.hasNextLine()){
								String line = sc1.nextLine();
								wineContent = wineContent.concat(line + "\n");
								String[] wineSplit = line.split("-");
								if (wineSplit[0].equals(comandoSplit[1]))
									erro = false;
							}
							sc1.close();
						} catch (Exception e) {
							
						}
						if(erro){
							outStream.writeObject("O vinho que tentou vender não está registado");
						}
						else{
							File wineSells;
							Scanner sc2;
							String sellContent = "";
							boolean adicionado = false;
							try {
								wineSells = new File("./Sells.txt");
								sc2 = new Scanner(wineSells);
								String linha = "";
								String[] linhaSplit;
								while (sc2.hasNextLine()){
									linha = sc2.nextLine();
									linhaSplit = linha.split("-");
									if (linhaSplit[1].equals(user) && linhaSplit[0].equals(comandoSplit[1])) {
										if (linhaSplit[2].equals(comandoSplit[2])) {
											adicionado = true;
											sellContent = sellContent.concat(comandoSplit[1] + "-" + user + "-" + comandoSplit[2] + "-" + (Integer.parseInt(comandoSplit[3]) + Integer.parseInt(linhaSplit[3])) + "\n");
										}
									} else {
										sellContent = sellContent.concat(linha + "\n");
									}
								}
								sc2.close();
								wr = new FileWriter("./Sells.txt");
							} catch (Exception e) {
								wr = new FileWriter("./Sells.txt");
							}
							wr.write(sellContent);
							if (!adicionado) {
								wr.write(comandoSplit[1] + "-" + user + "-" + comandoSplit[2] + "-" + comandoSplit[3]);
							}							
							outStream.writeObject("Vinho " + comandoSplit[1] + " adicionado a lista de vinhos a vender");
							wr.close();
						}
					}

					if (comandoSplit[0].equals("view") || comandoSplit[0].equals("v")) {
						erro = true;
						String[] wine = null;
						try {
							wineList = new File("./Wines.txt");
							sc1 = new Scanner(wineList);
							while(sc1.hasNextLine()){
								String line = sc1.nextLine();
								String[] wineSplit = line.split("-");
								if (wineSplit[0].equals(comandoSplit[1])){
									erro = false;
									wine = wineSplit.clone();
								}
							}
							sc1.close();

						} catch (Exception e) {
							
						}
						if(erro){
							outStream.writeObject("O vinho que tentou ver não está registado");
						}
						else{
							File wineSells;
							Scanner sc2;
							String line;
							int count = 0;
							try {
								wineSells = new File("./Sells.txt");
								sc2 = new Scanner(wineSells);
								while (sc2.hasNextLine()){
									line = sc2.nextLine();
									String[] wineSplit = line.split("-");
									if (wineSplit[0].equals(comandoSplit[1])) 
										count += Integer.parseInt(wineSplit[3]);
								}
								sc2.close();
								int stars = 0;
								int i = 0;
								String[] clasf = wine[2].split(":");
								while (i + 1 < clasf.length) {
									stars += Integer.parseInt(clasf[i+1]);
									i++;
								}
								if(i != 0)
									stars = stars / i;
								outStream.writeObject(wine[0] + " " + wine[1] + " quantidade disponivel: " + count + " Stars: " + stars);
							} catch (Exception e) {
								outStream.writeObject(wine[0] + " " + wine[1] + " quantidade disponivel: 0 Stars: NA");
							}
						}
					}

					if (comandoSplit[0].equals("buy") || comandoSplit[0].equals("b")) {
						File wineSells;
						String sellContent = "";

						String[] result = null;
						boolean exist = false;
						try {
							wineSells = new File("./Sells.txt");
							sc1 = new Scanner(wineSells);
							while (sc1.hasNextLine()) {
								String line = sc1.nextLine();
								String[] lineSplit = line.split("-");
								if (comandoSplit[1].equals(lineSplit[0]) && comandoSplit[2].equals(lineSplit[1])) {
									result = lineSplit.clone();
									exist = true;
									int q = Integer.parseInt(lineSplit[3]) - Integer.parseInt(comandoSplit[3]);
									sellContent = sellContent.concat(lineSplit[0] + "-" + lineSplit[1] + "-" + lineSplit[2] + "-" + q + "\n");
								}else
									sellContent = sellContent.concat(line + "\n");
							}
							sc1.close();
						} catch (Exception e) {
							
						}
						if (exist) {
							if (Integer.parseInt(result[3]) >= Integer.parseInt(comandoSplit[3])) {
								File wallet;
								String wContent = "";
								Scanner sc2;
								int saldoB = 0;
								int saldoS = 0;
								try {
									wallet = new File("./wallet.txt");
									sc2 = new Scanner(wallet);
									while (sc2.hasNextLine()) {
										String line = sc2.nextLine();
										String[] lineSplit = line.split("-");
										if (lineSplit[0].equals(user)) {
											saldoB = Integer.parseInt(lineSplit[1]);
										}else {
											if (lineSplit[0].equals(comandoSplit[2])) {
												saldoS = Integer.parseInt(lineSplit[1]);
											} else 
												wContent = wContent.concat(line + "\n");
										}
									}
									sc2.close();
								} catch (Exception e) {
									
								}
								int valor = (Integer.parseInt(result[2]) * Integer.parseInt(comandoSplit[3]));
								if (saldoB >= valor) {
									FileWriter wrW;
									FileWriter wrS;
									try {
										wrS = new FileWriter("./Sells.txt");
										wrS.write(sellContent);
										wrW = new FileWriter("./wallet.txt");
										wrW.write(wContent);
										saldoB -= valor;
										saldoS += valor;
										wrW.write(user + "-" + saldoB + "\n");
										wrW.write(comandoSplit[2] + "-" + saldoS + "\n");
										wrW.close();
										wrS.close();
										outStream.writeObject("Compra realizada");
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else 
									outStream.writeObject("Não tem saldo suficiente");
							}
							else
								outStream.writeObject("Não está disponivel quantidade suficiente");
						} else
						outStream.writeObject("Vinho não está a venda");
					}

					if (comandoSplit[0].equals("wallet") || comandoSplit[0].equals("w")) {
						File wallet;

						String result = "";
						try {
							wallet = new File("./wallet.txt");
							sc1 = new Scanner(wallet);
							while (sc1.hasNextLine()) {
								 String line = sc1.nextLine();
								 String[] wineSplit = line.split("-");
								 if (user.equals(wineSplit[0])) {
									result = result.concat(wineSplit[1]);
								 }
							}
							sc1.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						outStream.writeObject(result);
					}

					if (comandoSplit[0].equals("classify") || comandoSplit[0].equals("c")) {
						erro = true;
						try {
							wineList = new File("./Wines.txt");
							sc1 = new Scanner(wineList);
							while(sc1.hasNextLine()){
								String line = sc1.nextLine();
								wineContent = wineContent.concat(line);
								String[] wineSplit = line.split("-");
								if (wineSplit[0].equals(comandoSplit[1])){
									erro = false;
									wineContent = wineContent.concat(":" + comandoSplit[2] + "\n");
								}
								else
									wineContent = wineContent.concat("\n");
							}
							sc1.close();
						} catch (Exception e) {

						}
						if(erro){
							outStream.writeObject("O vinho que tentou classificar não está registado");
						}
						else{
							wr = new FileWriter("./Wines.txt");
							wr.write(wineContent);
							wr.close();
							outStream.writeObject("Vinho classificado");
						}
					}

					if (comandoSplit[0].equals("talk") || comandoSplit[0].equals("t")) {
						File usersIDs;
						String line;
						erro = true;
						try {
							usersIDs = new File("./clientIDs.txt");
							sc1 = new Scanner(usersIDs);
							while (sc1.hasNextLine()) {
								line = sc1.nextLine();
								String[] lineSplit = line.split(":");
								if (lineSplit[0].equals(comandoSplit[1])) {
									erro = false;
								}
							}
							sc1.close();
						} catch (Exception e) {
						}
						if(erro){
							outStream.writeObject("User não está registado");
						}
						else{
							File pm;
							Scanner sc2;

							String messageContent = "";
							try {
								pm = new File("./PM.txt");
								sc2 = new Scanner(pm);
								while(sc2.hasNextLine())
									messageContent = messageContent.concat(sc2.nextLine() + "\n");
								sc2.close();
								wr = new FileWriter("./PM.txt");
							} catch (Exception e) {
								wr = new FileWriter("./PM.txt");
							}
							String message = "";
							int i = 2;
							while(i < comandoSplit.length){
								message = message.concat(comandoSplit[i] + " ");
								i++;
							}
							wr.write(messageContent + user + "-" + comandoSplit[1] + "-" + message);
							wr.close();
							outStream.writeObject("Mensagem enviada");
						}
					}

					if (comandoSplit[0].equals("read") || comandoSplit[0].equals("r")) {
						File pm;
						String line;
						String pmContent = "";
						erro = true;
						String result = "";
						try {
							pm = new File("./PM.txt");
							sc1 = new Scanner(pm);
							while (sc1.hasNextLine()) {
								line = sc1.nextLine();
								String[] lineSplit = line.split("-");
								if (lineSplit[1].equals(user)) {
									if (!erro)
										result = result.concat("\n");
									result = result.concat(lineSplit[0] + ": " + lineSplit[2]);
									erro = false;
								}
								else
									pmContent = pmContent.concat(line);
							}
							sc1.close();
						} catch (Exception e) {
						}
						if (erro) {
							outStream.writeObject("Não tem mensagens na sua caixa");
						} else {

							try {
								wr = new FileWriter("./PM.txt");
								wr.write(pmContent);
								wr.close();
								outStream.writeObject(result);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
					}

					if (comandoSplit[0].equals("exit") || comandoSplit[0].equals("e")) {
						continua = false;
						System.out.println("User " + user + " desconectado");
						outStream.writeObject("Foi desconectado com sucesso");
					}

				}

				outStream.close();
				inStream.close();
 			
				socket.close();

			} catch (IOException | ClassNotFoundException e) {

				e.printStackTrace();

			}
		}
	}
}