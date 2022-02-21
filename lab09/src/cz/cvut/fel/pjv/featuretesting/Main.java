package cz.cvut.fel.pjv.featuretesting;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerSocket servSock = new ServerSocket(10000);

        Socket cliSock = servSock.accept();
        InputStream is = cliSock.getInputStream();
        OutputStream os = cliSock.getOutputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        OutputStreamWriter bufferedWriter = new OutputStreamWriter(os);

        String data = bufferedReader.readLine();
        System.out.println("Data received.");

        is.close();
        os.close();
        cliSock.close();
    }
}
