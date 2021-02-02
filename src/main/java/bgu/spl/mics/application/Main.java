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
 * This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
    public static CountDownLatch latch;
    public static CountDownLatch terLatch;


    public static void main(String[] args) {
        Diary.getInstance(); //Diary init
        latch=new CountDownLatch(4); //for Leia to start sending messages
        terLatch=new CountDownLatch(5); //for output file generation

        //input
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

        //ms init
        Thread Leia = new Thread(new LeiaMicroservice(input.getAttacks()));
        Thread C3PO = new Thread(new C3POMicroservice());
        Thread HanSolo = new Thread(new HanSoloMicroservice());
        Thread R2D2 = new Thread(new R2D2Microservice(input.getR2D2()));
        Thread Lando = new Thread(new LandoMicroservice(input.getLando()));

        //ms start
        Leia.start();
        C3PO.start();
        HanSolo.start();
        R2D2.start();
        Lando.start();

        //output
        try{
            terLatch.await();
            try (FileWriter writer = new FileWriter("Output.json")) {
                gson.toJson(Diary.getInstance(), writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(gson.toJson(Diary.getInstance())); //for debug
            System.out.println("difference: "+(Diary.getInstance().getC3POTerminate()-Diary.getInstance().getC3POFinish()));
        }catch (InterruptedException e){}
    }
}
