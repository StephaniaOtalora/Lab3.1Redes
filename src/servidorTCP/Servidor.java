package servidorTCP;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Servidor {

	public static int NUMERO_CONEXIONES;
	public final static String ARCHIVO_1 = "100mib.pdf";
	public final static String ARCHIVO_2 = "250mib.mp4";
	public static String[] respuestasClientes;
	public final static String UBICACION_LOG = "data/logs/log_";
	public static BufferedWriter writer;

	public static void main(String argv[]) {
		
		//Socket
		ServerSocket socket;
		
		//Sirve para leer el input de los usuarios
		Scanner leeLaConsola = new Scanner(System.in);
		try {
			
			//Crea un archivo .log con la fecha y hora en la que comienza a correr el servidor como nombre
			String time = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Calendar.getInstance().getTime());
			File log = new File(UBICACION_LOG + time + ".txt");
			
			//Agrega la fecha y hora inicial dentro del log
			writer = new BufferedWriter(new FileWriter(log));
			String fecha_hora = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime());
			writer.write("Fecha y hora: " + fecha_hora);
			writer.newLine();
			writer.flush();
			
			socket = new ServerSocket(9999);

			System.out.println("Ingrese el numero de conexiones que se van a manejar: ");
			int num = leeLaConsola.nextInt();
			NUMERO_CONEXIONES = num;

			writer.write("Conexiones a realizar para la prueba: " + NUMERO_CONEXIONES);
			writer.newLine();
			writer.flush();
			
			
			System.out.println(
					"Escriba el número del archivo que quiere enviar: \n(1) archivo de 100 MiB o \n(2) archivo de 250 MiB");
			int opcion = leeLaConsola.nextInt();
			leeLaConsola.close();
			if (opcion == 1) writer.write("Se va a realizar el envío del archivo: " + ARCHIVO_1);
			else writer.write("Se va a realizar el envío del archivo: " + ARCHIVO_2);
			writer.newLine();
			writer.flush();

			int nClients = 0;
			Socket[] sockets = new Socket[NUMERO_CONEXIONES];
			
			
			System.out.println("Esperando clientes...");
			while (nClients < NUMERO_CONEXIONES) {
				try {
					sockets[nClients] = socket.accept();
					DataOutputStream dataOut = new DataOutputStream(sockets[nClients].getOutputStream());
					DataInputStream dataIn = new DataInputStream(sockets[nClients].getInputStream());
					if (opcion == 2) {
						dataOut.writeByte(1);
						dataOut.writeUTF(ARCHIVO_2);
						dataOut.flush();
					} else {
						dataOut.writeByte(1);
						dataOut.writeUTF(ARCHIVO_1);
						dataOut.flush();
					}
					nClients++;
					if (dataIn.readByte() == 2)
						System.out.println("Llegó el cliente número " + nClients + " y está esperando el envío del archivo");
					dataOut.write(nClients);
					writer.write("Cliente número " + nClients + " conectado.");
					writer.newLine();
					writer.flush();
				}
				catch (Exception e){
					System.out.println("Hubo un problema.");
				}
			}
			System.out.println("Se comenzará el envio de archivos a los clientes");
			for (int i = 0; i < sockets.length; i++) {
				Hilo hilo = new Hilo(sockets[i],opcion, writer, (i + 1));
				hilo.start();
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
			try {
				writer.write("Ocurrió un error");
				writer.newLine();
				writer.flush();
			} catch (Exception ex) {}
		}
	}
}