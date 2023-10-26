package com.dinossauroProductions.pacotin;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.Objects;
import java.util.Random;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class Game extends Canvas implements Runnable, KeyListener, MouseListener, WindowListener {


    //  BASICOS DO SISTEMA
    @Serial
    private static final long serialVersionUID = 1L;
    public JFrame frame;
    private Thread thread;
    public boolean isRunning = true;
    public static final int WIDTH = 320 + 2;  //640 * 2 = 1280   16
    public static final int HEIGHT = 320 + 64 + 2; //380 * 2 = 720    9
    public static final double SCALE = 1;
    public int maxFPS = 60;
    @SuppressWarnings("FieldMayBeFinal")
    private BufferedImage image;
    public int FPS = 0;
    public static Random rand;


    //  VARIAVEIS DE JOGO
    public Cell[][] campo;
    public int boardSize;
    private int flaggedMines;
    private final int totalMines;
    public static final String RUNNING = "R", GAMEOVER = "G", VICTORY = "V";
    public String state;
    private boolean clicked = false;
    private int clickType = 0;
    private int inputX = 0, inputY = 0;
    private boolean newGame = false;
    private int secondsElapsed = 0;


    public Game(int tamanhoMapa, int qtdMinas){

        //  CARREGAR IMAGENS NO JOGO
        ssDisplay = new BufferedImage[10];

        try{
            BufferedImage spritesheet = ImageIO.read(getClass().getResource("/minesweeper_spritesheet.png"));
            flagImage = spritesheet.getSubimage(0 , 0, 32, 32);
            mineImage = spritesheet.getSubimage(32, 0, 32, 32);
            smileFace = spritesheet.getSubimage(64, 0, 32, 32);
            victoryScreen = spritesheet.getSubimage(0, 80, 256, 64);
            for(int i = 0; i < 10; i++){
                ssDisplay[i] = spritesheet.getSubimage(i * 26, 32, 26, 46);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //  PREPARACAO DO PROGRAMA

        addKeyListener(this);
        addMouseListener(this);
        setPreferredSize(new Dimension((int)(WIDTH*SCALE),(int)(HEIGHT*SCALE)));
        initFrame();
        frame.addWindowListener(this);


        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        rand = new Random();


        //  INICIO DA LÓGICA DO JOGO

        boardSize = tamanhoMapa;
        flaggedMines = totalMines = qtdMinas;
        newGame = false;
        campo = new Cell[boardSize][boardSize];
        state = RUNNING;
        Cell.generateMap(campo, boardSize, flaggedMines);
        secondsElapsed = 0;

    }

    public void initFrame() {
        frame = new JFrame("Campo Minado");
        frame.add(this);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setIconImage(mineImage);
        frame.setVisible(true);


    }

    public synchronized void start() {
        thread = new Thread(this);
        isRunning = true;
        thread.start();
    }

    public synchronized void stop() {
        isRunning = false;
        try{
            thread.join(1);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        int frames = 0;
        double timer = System.currentTimeMillis();
        requestFocus();
        while(isRunning){
            long now = System.nanoTime();
            delta+= (now - lastTime) / ns;
            lastTime = now;
            if(delta >= 1) {
                tick();
                render();
                frames++;
                delta--;
            }
            if(System.currentTimeMillis() - timer >= 1000){

                FPS = frames;
                frames = 0;
                timer+=1000;
                if(!state.equals(VICTORY))
                    secondsElapsed++;

            }
        }
        stop();
    }

    public void tick() {

        //aplicar lógica

        if(hasWon(campo)){
            state = VICTORY;
        }

        if(clicked){

            Cell cell = null;

            try{
                cell = campo[inputX][inputY];
            }catch(Exception ignored){}

            clicked = false;

            if(state.equals(RUNNING)){
                switch(clickType){

                    case 1 -> {
                        if(cell != null){
                            if(!cell.isFlagged()){

                                cell.revealCell(campo, inputX, inputY);

                                if(cell.getCelula() == -1){
                                    state = GAMEOVER;
                                }
                            }
                        }
                    }
                    case 3 -> {
                        //System.out.println("Botão 2 processado");
                        //cell.setFlagged(!cell.isFlagged());

                        if(cell.getClicked())
                            break;

                        if(cell.isFlagged()){
                            cell.setFlagged(false);
                            flaggedMines++;
                        }
                        else{
                            cell.setFlagged(true);
                            flaggedMines--;
                        }
                    }
                }

            }
            clickType = 0;
        }
    }

    private boolean hasWon(Cell[][] field){

        int nonBombs = (boardSize * boardSize) - totalMines;
        int clearedCells = 0;

        for (Cell[] cells : field) {
            for (int y = 0; y < field.length; y++) {
                if (cells[y].getClicked() && cells[y].getCelula() != -1) {
                    clearedCells++;
                }
            }
        }
        return nonBombs == clearedCells;

        /*
        int bombsConfirmed = 0;

        for(int x = 0; x < boardSize; x++){
            for(int y = 0; y < boardSize; y++){
                if((field[x][y].getCelula() == -1) && (field[x][y].isFlagged()))
                    bombsConfirmed++;
            }
        }
        //System.out.println(bombsConfirmed + " == "+totalMines +" -> " + (bombsConfirmed == totalMines));
        return bombsConfirmed == totalMines;

         */
    }

    private void processClick(MouseEvent e){

        int x = e.getX();
        int y = e.getY();

        if(y < 40){
            //g.drawImage(smileFace, WIDTH / 2 - 32 / 2, Y_UI_OFFSET / 2 - 32 / 2, null);

            Rectangle collisionSmileFace = new Rectangle(WIDTH / 2 - 32 / 2, Y_UI_OFFSET / 2 - 32 / 2, 32, 32);
            if(collisionSmileFace.contains(x, y) && e.getButton() == 1){
                newGame = true;
                Main.startNewGame = true;
            }

        }else if(Objects.equals(state, RUNNING) || Objects.equals(state, VICTORY)){
            x -= X_UI_OFFSET;
            y -= Y_UI_OFFSET;

            x /= 32;
            y /= 32;
        }

        clicked = true;
        clickType = e.getButton();
        inputX = x;
        inputY = y;

        //System.out.println("Clique na posição: \""+ x + ", "+ y +"\", com tipo de clique: "+ clickType +".");
    }

    //  VARIAVEIS DE RENDERIZAÇÃO

    private final Color[] cores = new Color[]{
            new Color(0xFF0000ff),  //1
            new Color(0xFF007b00),  //2
            new Color(0xFFff0000),  //3
            new Color(0xFF00007b),  //4
            new Color(0xFF7b0000),  //5
            new Color(0xFF00807f),  //6
            new Color(0xFF000000),  //7
            new Color(0xFF808080)   //8
    };

    private final Color
            CINZA_BG = new Color(0xFFbdbdbd),
            CINZA_GRID = new Color(0xFF7b7b7b),
            WHITE_GRID = new Color(0xFFFFFFFF);

    private final int X_UI_OFFSET = 0, Y_UI_OFFSET = 64;

    private static BufferedImage mineImage, flagImage, smileFace, victoryScreen;
    private static BufferedImage[] ssDisplay;

    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = image.getGraphics();


        //render stuff


        g.setColor(WHITE_GRID);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFont(new Font("Levi Windows", Font.BOLD, 32));


        g.setColor(CINZA_BG);
        g.fillRect(0, Y_UI_OFFSET, WIDTH, HEIGHT-Y_UI_OFFSET);


        //  RENDERIZAR CELULAS

        for(int x = 0; x < boardSize; x++){
            for(int y = 0; y < boardSize; y++) {

                Cell currentCell = campo[x][y];

                if(currentCell.getClicked()){
                    int valor = currentCell.getCelula();

                    if(valor >= 1){

                        g.setColor(cores[valor - 1]);
                        g.drawString(String.valueOf(valor), (x) * 32 + 8, (y) * 32 + Y_UI_OFFSET + 28);

                    }
                    else if(valor == -1){
                        g.drawImage(mineImage, (x) * 32 + X_UI_OFFSET, (y) * 32 + Y_UI_OFFSET, null);
                    }
                }
                else{

                    renderHiddenCell(g, x, y);

                    if(currentCell.isFlagged())
                        g.drawImage(flagImage, (x) * 32 + X_UI_OFFSET, (y) * 32 + Y_UI_OFFSET, null);

                }

                renderGrid(g, (x) * 32, (y) * 32 + Y_UI_OFFSET);


            }
        }

        //  RENDERIZAR FUNDO DAS CELULAS

        g.setColor(CINZA_GRID);
        g.fillRect(WIDTH - 2, Y_UI_OFFSET, 2, HEIGHT - Y_UI_OFFSET - 1);
        g.fillRect(0, HEIGHT - 2, WIDTH, 2);



        //  RENDERIZAR UI

        g.setColor(new Color(0xFFbdbdbd));
        g.fillRect(0, 0, WIDTH, Y_UI_OFFSET);

        renderNumberDisplay(g, Math.abs(flaggedMines), 24, 10);
        renderNumberDisplay(g, secondsElapsed, WIDTH - (26 * 3) - 24 + 1, 10);

        if(!state.equals(RUNNING)){
            int popupX = WIDTH / 2 - victoryScreen.getWidth() / 2;
            int popupY = HEIGHT / 2 - victoryScreen.getHeight() / 2;
            g.drawImage(
                    victoryScreen,
                    popupX,
                    popupY,
                    null);
            if(state.equals(VICTORY)){
                g.setColor(cores[0]);
                g.drawString("Victory!", popupX + (victoryScreen.getWidth() / 4), popupY + (victoryScreen.getHeight() * 2 / 3));
            }
            else if(state.equals(GAMEOVER)){
                g.setColor(cores[2]);
                g.drawString("Defeat!", popupX + (victoryScreen.getWidth() * 2 / 7), popupY + (victoryScreen.getHeight() * 2 / 3));

            }
        }



        g.drawImage(smileFace, WIDTH / 2 - 32 / 2, Y_UI_OFFSET / 2 - 32 / 2, null);

        //stop render


        g.dispose();
        g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, (int)(WIDTH*SCALE), (int)(HEIGHT*SCALE), null);

        bs.show();

    }

    private void renderNumberDisplay(Graphics g, int value, int x, int y){

        int number = value;

        int d1 = (number / 1  ) % 10;
        number -= d1;
        int d2 = (number / 10 ) % 10;
        number -= d2;
        int d3 = (number / 100) % 10;

        g.drawImage(ssDisplay[d3], x + 26 * 0, y, null);
        g.drawImage(ssDisplay[d2], x + 26 * 1, y, null);
        g.drawImage(ssDisplay[d1], x + 26 * 2, y, null);
    }

    private void renderGrid(Graphics g, int x, int y){

        g.setColor(CINZA_GRID);

        g.fillRect(x, y, 2, 32);
        g.fillRect(x + 2, y, 30, 2);

    }

    private void renderHiddenCell(Graphics g, int x, int y){

        g.setColor(WHITE_GRID);
        g.fillRect(
                x * 32,
                y * 32 + Y_UI_OFFSET + 2,
                30,
                2);
        g.fillRect(x * 32, y * 32 + Y_UI_OFFSET + 4, 28, 2);

        g.fillRect(x * 32 + 2, y * 32 + Y_UI_OFFSET, 2, 30);
        g.fillRect(x * 32 + 4, y * 32 + Y_UI_OFFSET, 2, 30);


        g.setColor(CINZA_GRID);
        g.fillRect(x * 32 + 4, y * 32 + Y_UI_OFFSET + 32 - 4, 28, 2);
        g.fillRect(x * 32 + 2, y * 32 + Y_UI_OFFSET + 32 - 2, 30, 2);

        g.fillRect(x * 32 + 32 - 4, y * 32 + Y_UI_OFFSET + 4, 2, 28);
        g.fillRect(x * 32 + 32 - 2, y * 32 + Y_UI_OFFSET + 2, 2, 26);

    }

    public void keyPressed(KeyEvent e) {

        /*
        if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            //up direction

        }
        if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            //down direction

        }


        if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
            //right direction

        }
        if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            //left direction

        }

         */

    }

    public void keyReleased(KeyEvent e) {

		/*
		if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {

		}
		*/

    }

    public void keyTyped(KeyEvent e) {

        if(e.getKeyCode() == KeyEvent.VK_A){
            isRunning = false;
        }

    }



    public void mouseClicked(MouseEvent e) {



    }

    public void mouseEntered(MouseEvent arg0) {


    }

    public void mouseExited(MouseEvent arg0) {


    }

    public void mousePressed(MouseEvent e) {

        processClick(e);

    }

    public void mouseReleased(MouseEvent arg0) {


    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {


    }

    @Override
    public void windowClosed(WindowEvent e) {

        isRunning = false;
        System.out.println("fechado");

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {


    }

    public static int generateRandom(int minimo, int maximo){
        return (Math.abs(rand.nextInt()) % (maximo - minimo) + 1) + minimo;
    }
}
