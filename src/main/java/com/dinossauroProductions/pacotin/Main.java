package com.dinossauroProductions.pacotin;

import java.util.ArrayList;

public class Main  {

    public static boolean startNewGame = true;

    public static ArrayList<Game> games;

    public static void main(String args[]){

        games = new ArrayList<>();

        do{
            if(startNewGame){
                System.out.println("Sexo 2");
                startNewGame = false;
                games.add(new Game(10, 15));
                games.get(games.size()-1).start();
            }

            if(games.size() == 0) {

                Thread.currentThread().getThreadGroup().interrupt();
                return;
                //break;
            }
                //System.out.println(Thread.activeCount());

            games.removeIf(game -> !game.isRunning);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }while(true);



    }

}
