package clienteTCP;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Cliente {

	private static final int TAMENSAJE = 1024;
	private static final String DESCARGA = "data/descargas/des_";
	public final static String LOG = "data/logs/log_";
	private static BufferedWriter bWriter;

	public static void main(String argv[]) {

		Scanner lector = new Scanner(System.in);

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		Socket socket;

		try {

			String time = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Calendar.getInstance().getTime());
			File logFile = new File(LOG + time + ".txt");
			bWriter = new BufferedWriter(new FileWriter(logFile));

			System.out.println("Indique el puerto en el que quiere hacer la conexión");
			int puerto = lector.nextInt();
			System.out.println("Escriba dirección ip del servidor");
			String direccion = lector.next();
			socket = new Socket(direccion, puerto);
			System.out.println("Conectado");
			System.out.println("Esperando nombre archivo");
			String nombreArchivo = "";
			DataInputStream dIn = new DataInputStream(socket.getInputStream());
			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
			int id = 0;
			if (dIn.readByte() == 1) {
				nombreArchivo = dIn.readUTF();
				System.out.println("Nombre del archivo: " + nombreArchivo);
				dOut.writeByte(2);
				id = dIn.readByte();
			}
			Cliente cli = new Cliente();
			cli.descargar(socket, nombreArchivo, id);

		}

		catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public void descargar(Socket socket, String nombreArch, int id) {
		int bytesRead = 0;
		int current = 0;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;

		PrintWriter escritor = null;
		BufferedReader lector = null;

		String inputLine;
		String outputLine;

		try {
			escritor = new PrintWriter(socket.getOutputStream(), true);
			lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String timeLog = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime());
			bWriter.write("Fecha y hora: " + timeLog);
			bWriter.newLine();
			bWriter.flush();

			// Recibir Hash
			DataInputStream dIn = new DataInputStream(socket.getInputStream());
			String hashRecibido = dIn.readUTF();

			bWriter.write("Se recibiran paquetes de " + TAMENSAJE + " bytes.");
			bWriter.newLine();
			bWriter.flush();

			byte[] bytearray = new byte[700000000];
			InputStream is = socket.getInputStream();

			fos = new FileOutputStream(DESCARGA + nombreArch);

			bos = new BufferedOutputStream(fos);
			while (bytesRead == 0) {
				bytesRead = is.read(bytearray, 0, bytearray.length);
			}
			System.out.println("Recibimos el archivo");
			current = bytesRead;

			
			long startTime = System.currentTimeMillis();

			int recibido = 0;
			int bytesRecibidos = 0;
			int numPaquetes = 0;
			do {
				bytesRead = is.read(bytearray, current, (bytearray.length - current));
				numPaquetes++;
				bytesRecibidos += bytesRead;
				if (bytesRecibidos >= TAMENSAJE) {

					recibido += (bytesRecibidos / TAMENSAJE);
					bytesRecibidos -= TAMENSAJE * (bytesRecibidos / TAMENSAJE);
				}
				if (bytesRead >= 0)
					current += bytesRead;
			} while (bytesRead > -1);

			bos.write(bytearray, 0, current);
			bos.flush();

			long endTime = System.currentTimeMillis();
			System.out.println("Archivo descargado (" + current + " bytes que fueron leidos)");

			System.out.println("La descarga se demoro " + (endTime - startTime) + " milisegundos");

			bWriter.write("Recibimos un total de " + numPaquetes + " paquetes.");
			bWriter.newLine();
			bWriter.flush();

			bWriter.write("Archivo descargado (" + current + " bytes que fueron leidos)");
			bWriter.newLine();
			bWriter.flush();

			bWriter.write("La descarga se demoro" + (endTime - startTime) + " milisegundos");
			bWriter.newLine();
			bWriter.flush();

			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
			dOut.writeByte(id);

			// Verificación del hash
			File myFile = new File(DESCARGA + nombreArch);
			byte[] bytesArchivo = new byte[(int) myFile.length()];
			byte[] hashSacado = new byte[1];
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			hashSacado = md.digest(bytesArchivo);
			String hashGenerado = new String(hashSacado);
			if (hashRecibido.equals(hashGenerado)) {
				System.out.println("Verificación del hash exitosa!");
				bWriter.write("Integridad de los archivos recibidos verificada");
				bWriter.newLine();
				bWriter.flush();
			} else
				System.out.println("Integridad del archivo fallida");

			escritor.close();
			lector.close();

			fos.close();
			bos.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			try {
				bWriter.write("Error de envío");
				bWriter.newLine();
				bWriter.flush();
			} catch (Exception ex) {
				// TODO: handle exception
			}
		}
	}

}