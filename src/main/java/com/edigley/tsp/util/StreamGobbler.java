package com.edigley.tsp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {

	private InputStream is;

	private String content = "";

	public StreamGobbler(InputStream is) {
		this.is = is;
	}

	public void run() {
		try {

			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				this.content += line + "\n";
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		// String command = "java -jar extrator-opendap.jar -d
		// CSMK3_SRA2_1_tasmax-change_2011-2030.nc -p pontos.txt -o
		// extracao.txt";
		// File dir = new File("/tmp/dir-1253798436-1280244308484/");

		String command = "zip -j -r extracoes.zip . -i ponto_*.txt";
		File dir = new File("/tmp/dir-407930607-1280256881514");

		System.out.println(command);

		Process process = Runtime.getRuntime().exec(command, new String[] {}, dir);
		boolean success = true;
		try {
			String retornoDoProcesso;
			System.out.println("process: " + process);

			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());

			FileOutputStream fos = new FileOutputStream("text.txt");
			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			if (process.waitFor() != 0) {
				System.out.println("Execução do Process falhou:");

				StringBuffer retornodoComando = new StringBuffer();
				String retorno;
				retornoDoProcesso = errorGobbler.getContent();

				success = false;
			} else {
				System.out.println("terminou a execução.");
				StringBuffer retornodoComando = new StringBuffer();
				String retorno;
				retornoDoProcesso = outputGobbler.getContent();
			}
			System.out.println("Saída do processo: \n" + retornoDoProcesso);
		} catch (InterruptedException e) {
			System.out.println("Execução do Process foi interrompida:");
			e.printStackTrace();
			success = false;
		}
	}

	public String getContent() {
		return content;
	}
}
