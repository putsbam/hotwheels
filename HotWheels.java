package wheels;
import robocode.*;

import static java.lang.System.out;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.*;

/**
 *
 * Inpired on an edit from sample.Crazy
 * College Project to UFPA
 *
 */

public class HotWheels extends AdvancedRobot {

    boolean movingForward;
    boolean inWall;

    public void run() {

        //  full black
        setBodyColor(Color.BLACK);
        setGunColor(Color.BLACK);
        setRadarColor(Color.BLACK);
        setBulletColor(Color.BLACK);
        setScanColor(Color.BLACK);


        setAdjustRadarForRobotTurn(true); // radar se move livremente do robô
        setAdjustGunForRobotTurn(true); // arma se move livremente do robô
        setAdjustRadarForGunTurn(true); // radar se move livremente da arma

        /**
        * para evitar de bater na parede (tomar dano)
         * quando chegar perto da parede, inWall recebe true
         * caso contrário, recebe false
         * basicamente para controlar a variável inWall inicialmente
        */

        if (getX() <= 50 || getY() <= 50 || getBattleFieldWidth() - getX() <= 50 || getBattleFieldHeight() - getY() <= 50) {
            inWall = true;
        } else {
            inWall = false;
        }

        setAhead(40000); // primeiro passo, só ir reto
        setTurnRadarRight(360); // rodar o radar em 360, até achar inimigo
        movingForward = true; // controlando a variavel movingForward, já que estamos movendo pra frente

        while (true) {
            /**
             * aqui também vai ser para controlar a variável inWall, porém durante a batalha
             * e, também, onde vai ser chamada a function reverseDirection, para mudar de direçao caso bata em uma parede
             */

            double xAxis = getX(); // posicao eixo X
            double yAxis = getY(); // posicao eixo y
            double btFieldWidth = getBattleFieldWidth(); // largura do campo de batalha
            double btFieldHeight = getBattleFieldHeight(); // altura do campo de batalha

            double resultX = btFieldWidth - xAxis; // distancia da parede no eixo x
            double resultY = btFieldHeight - yAxis; // distancia da parede no eixo y
            int distance = 50; // distancia (em pixels)

            /** Longe da parede (inWall recebe false) */
            if (xAxis > distance && yAxis > distance && resultX > distance && resultY > distance && inWall == true) {
                inWall = false;
            }

            /** Perto da parede (inWall recebe true e reverseDirection acionado) */
            if (xAxis <= distance || yAxis <= distance || resultX <= distance || resultY <= distance ) {
                if ( inWall == false){
                    reverseDirection();
                    inWall = true;
                    out.println("Oops, parede! trocando direção...");
                }
            }

            if (getRadarTurnRemaining() == 0.0){
                setTurnRadarRight(360); // girar radar em 360
            }

            execute(); // execute all actions set.

        }
    }

    /**
     * function para reverter a direcao do robot
     */
    public void reverseDirection() {
        if (movingForward) {
            setBack(40000);
            movingForward = false;
        } else {
            setAhead(40000);
            movingForward = true;
        }
    }

    /**
     * caso bata na parede, o evento é triggered e a function reverseDirection() é acionada
     */
    public void onHitWall(HitWallEvent e) {
        // Bounce off!
        reverseDirection();
    }

    /**
     * caso bata em um robô, reverte a direcao
     */
    public void onHitRobot(HitRobotEvent e) {
        if (e.isMyFault()) {
            out.println("Bati no cara, mudando direção...");
            reverseDirection();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // local do inimigo
        double distTotal = getHeading() + e.getBearing();
        double distGun = normalRelativeAngleDegrees(distTotal - getGunHeading());
        double distRadar = normalRelativeAngleDegrees(distTotal - getRadarHeading());

        // movimento em espiral, entre 80 e 100 graus
        if (movingForward) {
            setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 80));
        } else {
            setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 100));
        }


        /**
         * Caso inimigo esteja proximo o suficiente para maximo dano
         */
        if (Math.abs(distGun) <= 4) {
            setTurnGunRight(distGun);
            setTurnRadarRight(distRadar);

            /**
             * para previnir que o robo fique disabled, salvamos 0.1 de energy
             * e só atiramos a uma distancia minima, com máximo dano
             */
            if (getGunHeat() == 0 && getEnergy() > .2) {
                fire(Math.min(4.5 - Math.abs(distGun) / 2 - e.getDistance() / 250, getEnergy() - .1));
            }
        }
        /**
         * Caso contrário, só escaneia de novo
         */
        else {
            setTurnGunRight(distGun);
            setTurnRadarRight(distRadar);
        }

        /**
         * Girar o radar novamente caso veja um robô
         */
        if (distGun == 0) {
            out.println("Olha o carinha ali, vou mirar nele");
            scan();
        }
    }

}
