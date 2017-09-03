package com.ivalentin.margolariak;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that fetches an online page.
 * 
 * @author Inigo Valentin
 *
 */
class FetchURL {
 
	//List of strings that will contain the output, line by line
	private List<String> output;
	private String url;


	/**
	 * Constructor.
	 */
	FetchURL(){
		output = new ArrayList<>();
	}


	/**
	 * Returns the content of the fetched page.
	 * 
	 * @return A List of strings containing the lines of the web page.
	 */
	List<String> getOutput(){
		return output;
	}


	/**
	 * Actually fetches the web page.
	 * 
	 * @param u The URL to fetch.
	 */
	public void Run(String u){
		url = u;
		Thread t =  new Thread() {
			public void run() {  
				URL textUrl;
				try {
					textUrl = new URL(url);
					BufferedReader bufferReader = new BufferedReader(new InputStreamReader(textUrl.openStream()));
					String StringBuffer;
					List<String> lines = new ArrayList<>();
					while ((StringBuffer = bufferReader.readLine()) != null) {
						lines.add(StringBuffer);
					}
					bufferReader.close();
					output = lines;
				}
				catch (Exception e) {
					e.printStackTrace();
					output.add(e.toString());
				}
			}
		};
		t.start();
		try {
			t.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	} 
}