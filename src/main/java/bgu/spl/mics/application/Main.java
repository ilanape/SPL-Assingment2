package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewok;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import bgu.spl.mics.application.passiveObjects.Input;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.CountDownLatch;

/**
 * This is the Main class of the application. It parses the input file,
 * creates the different components of the application, and runs the system.
 * In the end, it outputs a JSON.
 */
public class Main {
    public static CountDownLatch subscribeLatch;
    public static CountDownLatch TerminateLatch;

    public static void main(String[] args) {
        Diary.getInstance(); //Diary init
        //for Leia to start sending messages
        // starts when all the others are subscribed to messages
        subscribeLatch =new CountDownLatch(4);
        //for output file generation
        TerminateLatch =new CountDownLatch(5);

        //input file parsing
        Gson gson = new Gson();
        Input input = new Input();
        try (Reader reader = new FileReader("input.json")) {
            input = gson.fromJson(reader, Input.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ewoks init
        Ewok[] arr=new Ewok[input.getEwoks()+1];
        for (int i = 1; i < arr.length; i++) {
            arr[i]=new Ewok(i);
        }
        Ewoks.getInstance().setEwoks(arr);

        //MS init
        Thread Leia = new Thread(new LeiaMicroservice(input.getAttacks()));
        Thread C3PO = new Thread(new C3POMicroservice());
        Thread HanSolo = new Thread(new HanSoloMicroservice());
        Thread R2D2 = new Thread(new R2D2Microservice(input.getR2D2()));
        Thread Lando = new Thread(new LandoMicroservice(input.getLando()));

        //MS start
        Leia.start();
        C3PO.start();
        HanSolo.start();
        R2D2.start();
        Lando.start();

        //output file creation
        try{
            TerminateLatch.await();

            try (FileWriter writer = new FileWriter("Output.json")) {
                gson.toJson(Diary.getInstance(), writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (InterruptedException e){}
    }
}
