/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.30.0.5071.d9da8f6cd modeling language!*/


/**
 * Board Class
 * A board is made up of position classes, which are then all stored in this class
 */
// line 90 "model.ump"
// line 183 "model.ump"
public class Board {

    //------------------------
    // MEMBER VARIABLES
    //------------------------


    //Board Associations
    private Position[][] positions = new Position[25][24]; // Board is in (y, x) format

    //------------------------
    // CONSTRUCTOR
    //------------------------


    public void addPosition(int y, int x, Position position) {
        positions[y][x] = position; // Board is in (y, x) format
    }

    public String toString() {
        StringBuilder boardPrint = new StringBuilder();
        for (Position[] position : positions) {
            for (int j = 0; j < positions[0].length; j++) {
                boardPrint.append("|").append(position[j].toString());
            }
            boardPrint.append("|\n");
        }
        return boardPrint.toString();
    }
}