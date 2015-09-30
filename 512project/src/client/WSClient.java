package client;

import server.*;
import server.ws.*;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.UnknownHostException;


public class WSClient {

	Socket clientSocket;
	
    public WSClient(String serviceName, String serviceHost, int servicePort) {
    
        try {
			clientSocket = new Socket(serviceHost, servicePort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
