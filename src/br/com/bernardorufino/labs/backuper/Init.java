package br.com.bernardorufino.labs.backuper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class Init {

    public static void main(String[] args) {
        System.out.println(DateTimeFormat.forPattern("M").print(DateTime.now()));
        System.out.println(DateTimeFormat.forPattern("ddMMyyyyHHmmssS").print(DateTime.now()));
        System.out.println(DateTimeFormat.forPattern("MMM").print(DateTime.now()));
        System.out.println(DateTimeFormat.forPattern("MMMM").print(DateTime.now()));



    }

}
