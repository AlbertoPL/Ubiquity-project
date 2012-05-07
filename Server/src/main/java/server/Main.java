package server;

import java.util.ArrayList;

public class Main { 
    public static void main (String[] args) {
        Server serv = new Server();
        serv.init();
        Thread	t  = new Thread(serv);
        t.start();
    }
}
