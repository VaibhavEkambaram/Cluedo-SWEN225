package Model;/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.30.0.5071.d9da8f6cd modeling language!*/

import View.AccusationMenu;
import View.SuggestionMenu;
import View.Table;

import java.util.*;

public class Game {

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    private States gameState = States.IDLE;
    private subStates subState;
    Table table;

    /**
     * Check if States are matching within their appropriate substates
     * If incorrect, throw an error with respective states
     */

    private final String[] weaponNames = {"Candlestick", "Dagger", "Lead Pipe", "Revolver", "Rope", "Spanner"};
    private final String[] roomNames = {"Kitchen", "Dining Room", "Lounge", "Hall", "Study", "Library", "Billiard Room", "Conservatory", "Ball Room"};
    private final String[] characterNames = {"Miss Scarlett", "Col. Mustard", "Mrs. White", "Mr. Green", "Mrs. Peacock", "Prof. Plum"};

    private final Map<String, WeaponCard> weaponCardsMap = new HashMap<>();
    private final Map<String, RoomCard> roomCardsMap = new HashMap<>();
    private final Map<String, CharacterCard> characterCardsMap = new HashMap<>();

    private List<Card> deck;
    private List<Room> rooms;
    public List<Player> players;

    private Board board;
    private Player currentPlayer;
    private Scenario murderScenario;  // Murder Scenario that players must find

    // track number of moves for current player
    int currentPlayerIndex = 0;
    int movesRemaining = -1;
    int numPlayers;


    /**
     * Game Constructor
     * Primary game springboard for all other methods of the game
     *
     * @author Cameron Li, Vaibhav Ekambaram
     */
    public Game() {
        initDeck();
        initPlayers();
        initBoard(); // generate board
        dealCards();
        runGame(); // main game logic loop
    }


    /**
     * Game Initialisation
     */


    /**
     * Create deck of cards and shuffle
     * Generate game murder scenario
     *
     * @author Cameron Li
     */
    private void initDeck() {
        transitionGameState(); // Transition from IDLE to INIT
        if (!subState.equals(subStates.DECK)) {
            throw new Error("Expecting DECK Sub State but " + subState);
        }

        // Adding cards
        deck = new ArrayList<>();

        // Weapons
        List<WeaponCard> weaponCards = new ArrayList<>();
        for (String weaponName : weaponNames) {
            WeaponCard weapon = new WeaponCard(weaponName);
            weaponCards.add(weapon);
            weaponCardsMap.put(weaponName, weapon);
            deck.add(weapon);
        }

        // Rooms
        rooms = new ArrayList<>();
        List<RoomCard> roomCards = new ArrayList<>();
        for (String r : roomNames) {
            Room newRoom = new Room(r);
            rooms.add(newRoom);
            RoomCard newRoomCard = new RoomCard(r, newRoom);
            roomCards.add(newRoomCard);
            roomCardsMap.put(r, newRoomCard);
            deck.add(newRoomCard);
        }

        // Characters
        List<CharacterCard> characterCards = new ArrayList<>();
        // Only create cards where there are players
        for (String c : characterNames) {
            CharacterCard character = new CharacterCard(c);
            characterCards.add(character);
            characterCardsMap.put(c, character);
            deck.add(character);
        }

        Collections.shuffle(deck);
        // Murder Scenario of Random Cards
        WeaponCard murderWeapon = weaponCards.get(new Random().nextInt(weaponNames.length - 1) + 1);
        RoomCard murderRoom = roomCards.get(new Random().nextInt(roomNames.length - 1) + 1);
        CharacterCard murderer = characterCards.get(new Random().nextInt(characterNames.length - 1) + 1);
        murderScenario = new Scenario(murderWeapon, murderRoom, murderer);
        System.out.println(murderScenario.toString());
        deck.remove(murderWeapon);
        deck.remove(murderRoom);
        deck.remove(murderer);
    }

    /**
     * Ask how many people want to play the game, then display character preferences menu
     * Using this information, set the players up for the game
     *
     * @author Vaibhav Ekambaram
     */
    private void initPlayers() {
        transitionSubState(); // Transition from DECK to PLAYERS

        if (!subState.equals(subStates.PLAYERS)) {
            throw new Error("Expecting PLAYERS Sub State but " + subState);
        }

        table = new Table(this);
        numPlayers = table.setPlayerCount();
        players = new ArrayList<>();
        players = table.setPlayers(characterNames, numPlayers, players, characterCardsMap);
    }

    /**
     * Load and create the Cluedo board
     * "x" = Forbidden Position
     * "_" = Standard Position
     * number = Model.Player starting Position
     * uppercase letter = Inner Room Position
     * lowercase letter = Outer Room Position
     * "d" + letter = Room Door Position
     *
     * @author Cameron Li
     */
    private void initBoard() {
        transitionSubState(); // Transition from DECK to BOARD

        if (!subState.equals(subStates.BOARD)) {
            throw new Error("Expecting BOARD Sub State but " + subState);
        }

        String boardLayout =
                " x x x x x x x x x 3 x x x x 4 x x x x x x x x x \n" +
                        " k k k k k k x _ _ _ b b b b _ _ _ x c c c c c c \n" +
                        " k K K K K k _ _ b b b B B b b b _ _ c C C C C c \n" +
                        " k K K K K k _ _ b B B B B B B b _ _ c C C C C c \n" +
                        " k K K K K k _ _ b B B B B B B b _ _ <C c C C c c \n" +
                        " k k K K K k _ _ <B B B B B B B >B _ _ _ c c c c x \n" +
                        " x k k k vK k _ _ b B B B B B B b _ _ _ _ _ _ _ 5 \n" +
                        " _ _ _ _ _ _ _ _ b vB b b b b vB b _ _ _ _ _ _ _ x \n" +
                        " x _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ i i i i i i \n" +
                        " d d d d d _ _ _ _ _ _ _ _ _ _ _ _ _ <I I I I I i \n" +
                        " d D D D d d d d _ _ x x x x x _ _ _ i I I I I i \n" +
                        " d D D D D D D d _ _ x x x x x _ _ _ i I I I I i \n" +
                        " d D D D D D D >D _ _ x x x x x _ _ _ i i i i vI i \n" +
                        " d D D D D D D d _ _ x x x x x _ _ _ _ _ _ _ _ x \n" +
                        " d D D D D D D d _ _ x x x x x _ _ _ l l ^L l l x \n" +
                        " d d d d d d vD d _ _ x x x x x _ _ l l L L L l l \n" +
                        " x _ _ _ _ _ _ _ _ _ x x x x x _ _ <L L L L L L l \n" +
                        " 2 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ l l L L L l l \n" +
                        " x _ _ _ _ _ _ _ _ h h ^H ^H h h _ _ _ l l l l l x \n" +
                        " o o o o o o ^O _ _ h H H H H h _ _ _ _ _ _ _ _ 6 \n" +
                        " o O O O O O o _ _ h H H H H >H _ _ _ _ _ _ _ _ x \n" +
                        " o O O O O O o _ _ h H H H H h _ _ ^Y y y y y y y \n" +
                        " o O O O O O o _ _ h H H H H h _ _ y Y Y Y Y Y y \n" +
                        " o O O O O o o _ _ h H H H H h _ _ y y Y Y Y Y y \n" +
                        " o o o o o o x 1 x h h h h h h x _ x y y y y y y \n";

        Board board = new Board();
        Scanner layoutScan = new Scanner(boardLayout);
        int y = -1;
        // Scan row (y)
        while (layoutScan.hasNextLine()) {
            String scanLine = layoutScan.nextLine(); // Scanned row (y)
            y++;
            int x = -1;
            Scanner positionScan = new Scanner(scanLine); // Scan Column (x)
            while (positionScan.hasNext()) {
                String positionName = positionScan.next(); // Scanned Column (x)
                x++; // Increment row
                Position newPosition = null;
                if (positionName.equals("x")) { // Check for "x", a forbidden position
                    newPosition = new Position(x, y, false);
                }
                if (positionName.equals("_")) { // Check for "_" the basic movable position
                    newPosition = new Position(x, y, true);
                }

                if (newPosition == null) { // If still haven't found anything
                    for (Room r : rooms) { // Check for a room
                        if (positionName.equals(r.getRoomChar())) { // Is this an inner room position?
                            newPosition = new Position(x, y, true, true, null, r);
                            break;
                        } else if (positionName.equals("^" + r.getRoomChar())) { // Up door
                            newPosition = new Position(x, y, true, true, Move.Direction.UP, r);
                            break;
                        } else if (positionName.equals(">" + r.getRoomChar())) { // Right door
                            newPosition = new Position(x, y, true, true, Move.Direction.RIGHT, r);
                            break;
                        } else if (positionName.equals("v" + r.getRoomChar())) { // Down door
                            newPosition = new Position(x, y, true, true, Move.Direction.DOWN, r);
                            break;
                        } else if (positionName.equals("<" + r.getRoomChar())) {
                            newPosition = new Position(x, y, true, true, Move.Direction.LEFT, r);
                            break;
                        } else if (positionName.equals(r.getRoomChar().toLowerCase())) { // Is this an outer room position?
                            newPosition = new Position(x, y, true, false, null, r);
                            break;
                        }
                    }
                }

                if (newPosition == null) { // Add in remaining Character Positions
                    for (CharacterCard c : characterCardsMap.values()) {
                        if (positionName.equals(c.getCharacterBoardChar())) { // Check if position is a character
                            for (Player p : players) { // Make sure that a player is playing the character
                                if (p.getCharacter().equals(c)) { // Case player for character exists, create Character position
                                    newPosition = new Position(x, y, true, c);
                                    p.setCurrentPosition(newPosition);
                                    break;
                                } else { // Else, is a basic movable position "_"
                                    newPosition = new Position(x, y, true);
                                }
                            }
                            break;
                        }
                    }
                }
                board.addPosition(y, x, newPosition); // Add found position
            }
            positionScan.close();
        }
        layoutScan.close();
        this.board = board; // Set board
    }

    /**
     * Deal Cards
     * Add cards from the deck list to a stack, then deal them to each player until the stack is empty
     *
     * @author Cameron Li
     */
    public void dealCards() {
        Stack<Card> toBeDealt = new Stack<>();
        for (Card card : this.deck) {
            toBeDealt.push(card);
        }

        while (!toBeDealt.isEmpty()) {
            for (Player p : this.players) {
                // Make sure not null pointer exception
                if (toBeDealt.isEmpty()) break;
                p.addHand(toBeDealt.pop());
            }
        }
    }


    /**
     * Core Gameplay
     */


    /**
     * Main Game Loop
     *
     * @author Cameron Li, Vaibhav Ekambaram
     */
    public void runGame() {
        initToRunning();
        movementTransition();
        table.updateDisplay();
        while (gameState.equals(States.RUNNING)) {
            table.updateDisplay();
        }
    }

    /**
     * Roll two dice and then return the overall number
     *
     * @return Cumulative dice throw result
     * @author Vaibhav Ekambaram
     */
    public int rollDice() {
        // find a random number in the range of 0 to 5, then add 1 as an offset for 1 to 6
        int firstResult = new Random().nextInt(6) + 1;
        int secondResult = new Random().nextInt(6) + 1;

        System.out.println("first dice throw: " + firstResult);
        System.out.println("second dice throw: " + secondResult);
        return firstResult + secondResult;
    }

    /**
     * Handle Movement
     * Check validity
     * Transition states if no more moves
     *
     * @param move
     * @return
     * @author Cameron Li
     */
    public boolean movementInput(Move move) {
        if (!subState.equals(subStates.MOVEMENT)) {
            throw new Error("Expected MOVEMENT sub state but : " + subState);
        }

        if (move.getSpaces() > movesRemaining) {
            return false;
        }
        Board newBoard = board.apply(currentPlayer, move);
        if (newBoard != null) {
            movesRemaining = movesRemaining - move.getSpaces();
            if (movesRemaining < 1) {
                actionTransition();
            }
            board = newBoard;
            return true;
        }

        return false;
    }

    /**
     * Handle Accusations
     *
     * @param p player
     * @return integer (-1 invalid, 0 failed, 1 successful)
     * @author Baxter Kirikiri, Vaibhav Ekambaram
     */
    public int makeAccusation(Player p) {
        AccusationMenu a = new AccusationMenu();
        if (!p.getCanAccuse()) {
            a.unableToAccuse(p);
            return -1;
        }

        String accusationString = a.makeAccusation(characterNames, weaponNames, roomNames);
        String[] accusationStringSplit = accusationString.split("\t");

        // create scenario and compare to the original murder scenario
        Scenario accusationScenario = new Scenario(weaponCardsMap.get(accusationStringSplit[1]), roomCardsMap.get(accusationStringSplit[2]), characterCardsMap.get(accusationStringSplit[0]));

        if (murderScenario.equals(accusationScenario)) {
            a.successfulAccusation(p, murderScenario);
            return 1;
        } else {
            a.incorrectAccusation(p);
            p.setCanAccuse(false);
            return 0;
        }
    }

    /**
     * Handles player suggestions.
     *
     * @author Baxter Kirikiri, Vaibhav Ekambaram, Cameron Li
     */
    public int makeSuggestion(Player p) {
        // TODO: Fix this
        SuggestionMenu s = new SuggestionMenu();


        //initialize required fields
        Room room;
        RoomCard suggestionRoom = null;
        WeaponCard suggestionWeapon;
        CharacterCard suggestionCharacter;


        // check if player is currently in a room, can not suggest if not
        if (p.getCurrentPosition().getRoom() == null) {
            s.unableToSuggest(p);
            return -1;
        }

        room = p.getCurrentPosition().getRoom();

        if (roomCardsMap.containsKey(p.getCurrentPosition().getRoom().toString())) {
            suggestionRoom = roomCardsMap.get(p.getCurrentPosition().getRoom().toString());
        }

        String suggestionString = s.makeSuggestion(characterNames, weaponNames, roomNames, Objects.requireNonNull(suggestionRoom));
        String[] suggestionStringSplit = suggestionString.split("\t");

        suggestionWeapon = weaponCardsMap.get(suggestionStringSplit[1]);
        suggestionCharacter = characterCardsMap.get(suggestionStringSplit[0]);


        System.out.println("Moving " + suggestionCharacter.getCharacterName() + " to " + room.toString() + "...");

        // teleport the suggested player to the suggested room
        for (Player findP : players) {
            if (findP.getCharacter().getCharacterName().equals(suggestionCharacter.getCharacterName())) {

                Room otherPlayerRoom = findP.getCurrentPosition().getRoom();

                if (otherPlayerRoom == null || !findP.getCurrentPosition().getRoom().equals(room)) {
                    Board newBoard = board.teleportPlayer(findP, room);
                    if (newBoard != null) {
                        board = newBoard;
                    } else {
                        s.teleportFailed();
                    }
                }
            }
        }

        System.out.println("\n" + this.board + "\n");

        Scenario suggestion = new Scenario(suggestionWeapon, suggestionRoom, suggestionCharacter);
        System.out.println(p.getCharacter().getCharacterName() + "'s suggestion: [" + suggestion.toString() + "]");

        // place all other players in a stack so that they can take turns at refuting
        Stack<Player> refuters = new Stack<>();
        for (Player addToStack : players) {
            if (!addToStack.equals(p)) {
                refuters.add(addToStack);
            }
        }

        // iterate through the stack, asking each player to produce a refutation.
        boolean refuted = false;
        while (!refuters.isEmpty()) {
            Player currentTurn = refuters.pop();

            s.makeRefutation(currentTurn, currentPlayer, suggestion);


            System.out.println(currentTurn.getCharacter().getCharacterName() + "'s turn to refute");
            System.out.println(currentTurn.getCharacter().getCharacterName() + "'s hand: ");

            List<Card> refuteCards = currentTurn.getHand();

            for (Card c : refuteCards) System.out.println("\t" + c.toString());

            System.out.println("Refute this suggestion with a card from your hand or type pass to skip: ");

            Card refutation = null;
            boolean failedRefutation = false;

            // read the input from the player and match it to a card in their hand. If no matching card exists, the player is prompted to try again
            while (refutation == null && !failedRefutation) {
                String refutationInput = new Scanner(System.in).nextLine();

                if (refutationInput.equalsIgnoreCase("Pass")) {
                    failedRefutation = true;
                } else {
                    for (Card c : refuteCards) {
                        if (c.toString().equals(refutationInput)) refutation = c;
                    }
                }

                if (refutation == null && !failedRefutation) {
                    System.out.println("Card not found in players hand! Please try again: ");
                }
            }

            // if the previous player failed to refute, continue the loop
            if (failedRefutation) continue;

            // if the player successfully refutes the suggestion, end the loop
            if (refutation.toString().equals(suggestionCharacter.toString()) || refutation.toString().equals(suggestionRoom != null ? suggestionRoom.toString() : null) || refutation.toString().equals(suggestionWeapon.toString())) {
                s.refuted(p.getCharacter().getCharacterName());
                refuted = true;
                break;
            } else { //if the card the player used to refute is in their hand but it does not match any of the suggested cards
                System.out.println("Invalid refutation!");
            }
        }

        //if the stack is empty and no other player could refute, offer the option of making an Accusation to player who suggested
        if (!refuted) {
            System.out.println(p.getCharacter().getCharacterName() + "'s turn");

            int i = s.nobodyCouldRefute();
            System.out.println(i);
            if (i == 0) {
                int accuse = makeAccusation(p);
                if (accuse == 1) {
                    transitionGameState();
                } else {
                    movementTransition();

                    table.setRollDiceButtonVisibility(true);
                }
            } else {
                System.out.println("next players turn");
            }
        }
        return 0;
    }

    /**
     * Switch current player to the next player
     */
    public void changePlayer() {
        if (currentPlayerIndex + 1 >= numPlayers) {
            currentPlayerIndex = 0;
        } else {
            currentPlayerIndex++;
        }
        currentPlayer = players.get(currentPlayerIndex);
    }

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
    private void idleToInit() {
        if (gameState.equals(States.IDLE)) {
            throw new Error("Expected IDLE game state but " + gameState);
        }
=======
    private void checkGameState() {
        if (gameState.equals(States.IDLE) && subState == null) {
            return;
        }
=======
    private void checkGameState() {
        if (gameState.equals(States.IDLE) && subState == null) {
            return;
        }
>>>>>>> parent of 497a1aa... State Transitions Finished
=======
    private void checkGameState() {
        if (gameState.equals(States.IDLE) && subState == null) {
            return;
        }
>>>>>>> parent of 497a1aa... State Transitions Finished
=======
    private void checkGameState() {
        if (gameState.equals(States.IDLE) && subState == null) {
            return;
        }
>>>>>>> parent of 497a1aa... State Transitions Finished

        if (gameState.equals(States.RUNNING)) { // Check Sub state matches RUNNING game state
            if (!subState.equals(subStates.MOVEMENT) && !subState.equals(subStates.ACTION)) {
                throw new Error(gameState + " game state with " + subState + " sub state");
            }
        } else if (gameState.equals(States.INIT)) { // Check Sub state matches INIT game state
            if (!subState.equals(subStates.PLAYERS) && !subState.equals(subStates.DECK) && !subState.equals(subStates.BOARD)) {
                throw new Error(gameState + " game state with " + subState + " sub state");
            }
        }

        // Check game state matches relevant sub states
        if (subState.equals(subStates.MOVEMENT) || subState.equals(subStates.ACTION)) {
            if (!gameState.equals(States.RUNNING)) {
                throw new Error(gameState + " game state with " + subState + " sub state");
            }
        } else if (subState.equals(subStates.PLAYERS) || subState.equals(subStates.DECK) || subState.equals(subStates.BOARD)) {
            if (!gameState.equals(States.INIT)) {
                throw new Error(gameState + " game state with " + subState + " sub state");
            }
        }

    }

    public void transitionGameState() {
        checkGameState();
        if (gameState.equals(States.IDLE)) {
            gameState = States.INIT;
            subState = subStates.DECK;
        } else if (gameState.equals(States.INIT)) {
            gameState = States.RUNNING;
            subState = subStates.MOVEMENT;
        } else if (gameState.equals(States.RUNNING)) {
            gameState = States.FINISHED;
        }
    }

    private void transitionSubState() {
        checkGameState();
        if (gameState.equals(States.RUNNING)) {
            if (subState.equals(subStates.MOVEMENT)) {
                subState = subStates.ACTION;
            } else {
                subState = subStates.MOVEMENT;
            }
        } else if (gameState.equals(States.INIT)) {
            if (subState.equals(subStates.DECK)) {
                subState = subStates.PLAYERS;
            } else {
                subState = subStates.BOARD;
            }
        }
    }

    private void idleToInit() {
        if (gameState.equals(States.IDLE)) {
            throw new Error("Expected IDLE game state but " + gameState);
        }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> parent of 497a1aa... State Transitions Finished
=======
>>>>>>> parent of 497a1aa... State Transitions Finished
=======
>>>>>>> parent of 497a1aa... State Transitions Finished
=======
>>>>>>> parent of 497a1aa... State Transitions Finished
        gameState = States.INIT;
    }

    private void initToRunning() {
        if (!gameState.equals(States.INIT)) {
            throw new Error("Expected INIT game state but " + gameState);
        }
        gameState = States.RUNNING;
    }

    public void movementTransition() {
        if (!gameState.equals(States.RUNNING)) {
            throw new Error("Expected RUNNING game state but : " + gameState);
        }
        if (subState.equals(subStates.ACTION)) {
            changePlayer();
        }

        subState = subStates.MOVEMENT;
        currentPlayer = players.get(currentPlayerIndex);
    }

    public void actionTransition() {
        if (!gameState.equals(States.RUNNING)) {
            throw new Error("Expected RUNNING game state but : " + gameState);
        }
        if (!subState.equals(subStates.MOVEMENT)) {
            throw new Error("Expected Movement sub state but : " + subState);
        }
        subState = subStates.ACTION;
    }

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
    public void finishTransition() {
        if (!gameState.equals(States.RUNNING)) {
            throw new Error("stuff");
        }
        gameState = States.FINISHED;
    }

=======
>>>>>>> parent of 497a1aa... State Transitions Finished
=======
>>>>>>> parent of 497a1aa... State Transitions Finished
=======
>>>>>>> parent of 497a1aa... State Transitions Finished
=======
>>>>>>> parent of 497a1aa... State Transitions Finished
    /**
     * Getters
     */


    public States getGameState() {
        return gameState;
    }

    public subStates getSubState() {
        return subState;
    }

    public Board getBoard() {
        return this.board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getMovesRemaining() {
        return movesRemaining;
    }

    /**
     * Setters
     */


    public void setMovesRemaining(int value) {
        this.movesRemaining = value;
    }

    /**
     * State Methods
     */


    // Main Game States
    public enum States {
        IDLE, INIT, RUNNING, FINISHED
    }


    // Sub States for Main Game States
    public enum subStates {
        PLAYERS, DECK, BOARD, MOVEMENT, ACTION
    }


    /**
     * System.out Core Methods
     */


    /**
     * MAIN GAME LOOP
     * This method contains the main game logic. After the game as been setup in the game constructor, this method then
     * loops through, carrying out the game functions
     *
     * @author Vaibhav Ekambaram
     */
    /*
    public void mainGameLoop() {


        while (gameState.equals(States.RUNNING)) {
            for (Player p : players) {
                movesRemaining = -1;
                System.out.println("**************************************************");
                System.out.println("Current Player: " + p.getCharacter().getCharacterName() + " (" + p.getCharacter().getCharacterBoardChar() + " on board)");
                currentPlayer = p;
                System.out.println("**************************************************");
                movesRemaining = rollDice();
                System.out.println("Result: " + movesRemaining);
                System.out.println("**************************************************");
                while (subState.equals(subStates.MOVEMENT)) {
                    if (p.getCurrentPosition().getRoom() != null) {
                        System.out.println("Currently in " + p.getCurrentPosition().getRoom());
                        System.out.println("**************************************************");
                    }
                    System.out.println("Please enter a move command or type \"finish\" to complete move (" + movesRemaining + " tiles remaining):");
                    Move move = movementInput(movesRemaining);
                    if (move != null) {
                        Board newBoard = this.board.apply(p, move);
                        if (newBoard != null) {
                            this.board = newBoard;
                            movesRemaining = movesRemaining - move.getSpaces();
                            System.out.println(board.toString());
                            table.updateDisplay(board.toString());
                        }
                    } else {
                        transitionSubState();
                        break;
                    }
                    if (movesRemaining < 1) {
                        transitionSubState(); // Transition from Movement to Action
                    }
                }
                System.out.println("\t\t________________________________");
                System.out.println("\t\tCurrent Players Hand:");
                System.out.println("\t\t________________________________");
                for (Card c : p.getHand()) {
                    if (c instanceof CharacterCard) {
                        System.out.println("\t\t[Character] " + c.toString());
                    } else if (c instanceof WeaponCard) {
                        System.out.println("\t\t[Weapon] " + c.toString());
                    } else if (c instanceof RoomCard) {
                        System.out.println("\t\t[Room] " + c.toString());
                    }
                }
                System.out.println("\t\t________________________________");
                System.out.println("Would you like to make a suggestion, accusation or pass?");
                System.out.println("Available commands - [suggestion][accusation][pass]:");
                table.setSuggestionAccusationVisibility(true);
                String answer = "";
                String input = new Scanner(System.in).nextLine();
                try {
                    answer = input;
                } catch (Exception e) {
                    System.out.println("Please enter 'Accusation', 'Suggestion', or 'Pass'");
                }
                if (answer.equalsIgnoreCase("a") || answer.equalsIgnoreCase("accusation")) {
                    int accuse = makeAccusation(p);
                    if (accuse == 1) {
                        gameState = States.FINISHED;
                        break;
                    }
                } else if (answer.equalsIgnoreCase("s") || answer.equalsIgnoreCase("suggestion")) {
                    makeSuggestion(p);
                }
                System.out.println("[Hit Enter to move to the next player]");
                Scanner wait = new Scanner(System.in);
                wait.nextLine();
                transitionSubState(); // Transition Action to Movement
            }
        }
    }
     */

    /**
     * Asks current player to perform an action
     * Returns a move to apply to the board
     *
     * @return Model.Move
     * @author Cameron Li, Vaibhav Ekambaram
     */
    /*
    public Move movementInput(int movesRemaining) {
        Move.Direction direction = null;
        int spaces = 0;
        Scanner inputScan = new Scanner(System.in);
        boolean valid = false;
        while (!valid) {
            String command = inputScan.nextLine();
            try {
                if (command.length() >= 4 && command.startsWith("up-")) {
                    direction = Move.Direction.UP;
                    spaces = Integer.parseInt(command.substring(3));
                } else if (command.length() >= 6 && command.startsWith("left-")) {
                    direction = Move.Direction.LEFT;
                    spaces = Integer.parseInt(command.substring(5));
                } else if (command.length() >= 7 && command.startsWith("right-")) {
                    direction = Move.Direction.RIGHT;
                    spaces = Integer.parseInt(command.substring(6));
                } else if (command.length() >= 6 && command.startsWith("down-")) {
                    direction = Move.Direction.DOWN;
                    spaces = Integer.parseInt(command.substring(5));
                } else if (command.equals("finish")) {
                    return null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a correct number of spaces");
            }

            if (direction != null && spaces <= movesRemaining) {
                valid = true;
            }

            if (!valid) {
                if (spaces > movesRemaining) {
                    System.out.println("Insufficient amount of moves left");
                } else {
                    System.out.println("Please enter a proper command");
                    System.out.println("Movement is \"direction-spaces\" - e.g. up-4");
                }
            }

        }

        return new Move(direction, spaces);
    }
     */

}