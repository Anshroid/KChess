package uk.co.anshroid.kchess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * The board for the game
 */
@SuppressWarnings("unchecked")
public class Board extends JPanel implements MouseListener {
    public static Dimension rootSize;

    public String destinationFile;
    public String player1;
    public String player2;
    public int result;

    public final Square[][] squares = new Square[8][8];
    private Square selectedPiece = null;
    private boolean whiteTurn = true;
    private boolean turnIsInCheck = false;
    private boolean promoting = false;
    private Square promotionSquare;

    public final HashMap<Boolean, Square> kings = new HashMap<>();

    public final HashMap<Boolean, Boolean> kingsideCastling = new HashMap<>();
    public final HashMap<Boolean, Boolean> queensideCastling = new HashMap<>();

    public final ArrayList<String> past = new ArrayList<>();
    public final ArrayList<String> future = new ArrayList<>();

    /**
     * Create a new board
     */
    public Board(Dimension rootSize, String player1, String player2) {
        super();

        Board.rootSize = rootSize;
        this.player1 = player1;
        this.player2 = player2;
        destinationFile = Util.rollSaveFilename(MainScreen.savePath, player1 + player2);

        // Set the size of the board
        //noinspection SuspiciousNameCombination
        setPreferredSize(new Dimension(rootSize.width, rootSize.width));
        setLayout(new GridLayout(8, 8));

        // Create the squares
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Square square = squares[i][j] = new Square(new Util.Tuple<>(j + 1, 8 - i), this);
                square.addMouseListener(this);
                add(square);
            }
        }

        resetBoard();
    }

    /** Handle mouse clicks on the board
     * @param e The mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (promoting) return;

        Square square = (Square) e.getSource();
        boolean squareHasPiece = square.piece != -1;
        boolean pieceIsSelected = selectedPiece != null;
        boolean isSquaresTurn = squareHasPiece && square.isWhite() == whiteTurn;
        boolean isMoveLegal = pieceIsSelected && selectedPiece.getLegalMoves().contains(square.pos);

        if (isMoveLegal) {
            // Check if the move was an en passant
            boolean enPassant = false;
            if (selectedPiece.piece == (whiteTurn ? 2 : 4) && !Objects.equals(selectedPiece.pos.x(), square.pos.x()) && square.enPassant) {
                getSquareAt(new Util.Tuple<>(square.pos.x(), selectedPiece.pos.y())).setPiece(-1);
                enPassant = true;
            }

            clearEnPassant();

            past.add(getMoveRepresentation(selectedPiece.pos, square.pos, kingsideCastling.get(whiteTurn), queensideCastling.get(whiteTurn), enPassant));
            future.clear();


            if (selectedPiece.piece == (whiteTurn ? 3 : 6)) {
                kings.put(whiteTurn, square); // Record the king's position
                kingsideCastling.put(whiteTurn, false); // Disable castling
                queensideCastling.put(whiteTurn, false);

                if (Math.abs(selectedPiece.pos.x() - square.pos.x()) == 2) {
                int rookX = square.pos.x() == 7 ? 8 : 1;
                int rookNewX = square.pos.x() == 7 ? 6 : 4;
                getSquareAt(new Util.Tuple<>(rookX, square.pos.y())).setPiece(-1);
                getSquareAt(new Util.Tuple<>(rookNewX, square.pos.y())).setPiece(whiteTurn ? 13 : 26);
                }
            }

            if (selectedPiece.piece == (whiteTurn ? 13 : 26)) {
                if (square.pos.x() == 8) {
                    // Disable Kingside castling
                    kingsideCastling.put(whiteTurn, false);
                }
                else if (square.pos.x() == 1) {
                    // Disable Queenside castling
                    queensideCastling.put(whiteTurn, false);
                }
            }


            // Check if the move is a two-forward pawn move
            if (selectedPiece.piece == (whiteTurn ? 2 : 4) && Math.abs(selectedPiece.pos.y() - square.pos.y()) == 2) {
                getSquareAt(new Util.Tuple<>(square.pos.x(), square.pos.y() + (whiteTurn ? -1 : 1))).enPassant = true;
            }


            // Move the piece
            square.setPiece(selectedPiece.piece);
            selectedPiece.setPiece(-1);
            selectedPiece = null;
            clearDots();

            // Check for pawn promotion
            if (square.piece == (whiteTurn ? 2 : 4) && square.pos.y() == (whiteTurn ? 8 : 1)) {
                promoting = true;
                promotionSquare = square;
                ((MainUI) getParent()).showPromotionMenu();
                return;
            }

            // End the turn
            whiteTurn = !whiteTurn;
            endTurn();
            checkMate();
            return;
        }

        if (isSquaresTurn) {
            // Select the piece
            selectedPiece = square;
            clearDots();
            for (Util.Tuple<Integer, Integer> move : selectedPiece.getLegalMoves()) {
                getSquareAt(move).setDot(true);
            }
        }
        else {
            // Deselect the piece
            selectedPiece = null;
            clearDots();
        }
    }

    /**
     * Check if it is a checkmate
     */
    private void checkMate() {
        for (Square[] row : squares) {
                for (Square piece : row) {
                    if (piece.isWhite() == whiteTurn && piece.getLegalMoves().size() > 0) {
                        return;
                    }
                }
        }

        if (turnIsInCheck) {
            ((MainUI) getParent()).endGame(whiteTurn ? EndGameReason.BLACK_WIN : EndGameReason.WHITE_WIN);
        } else {
            ((MainUI) getParent()).endGame(EndGameReason.STALEMATE);
        }
    }

    /**
     * Promote a pawn
     * @param piece The piece to promote to
     */
    public void promote(String piece) {
        switch (piece) {
            case "♛":
                promotionSquare.setPiece(whiteTurn ? 5 : 10);
                break;
            case "♜":
                promotionSquare.setPiece(whiteTurn ? 13 : 26);
                break;
            case "♝":
                promotionSquare.setPiece(whiteTurn ? 7 : 14);
                break;
            case "♞":
                promotionSquare.setPiece(whiteTurn ? 11 : 22);
                break;
        }

        past.set(past.size() - 1, past.get(past.size() - 1) + "p" + promotionSquare.piece);

        promoting = false;
        promotionSquare = null;
        whiteTurn = !whiteTurn;
        endTurn();
        checkMate();
    }

    private void endTurn() {
        clearChecks();
        turnIsInCheck = computeChecks(whiteTurn, kings.get(whiteTurn).pos);
        if (turnIsInCheck) {
            kings.get(whiteTurn).setCheck(true);
        }
    }

    /**
     * Clear the check flags from the board
     */
    private void clearChecks() {
        for (Square[] row : squares) {
            for (Square square : row) {
                square.check = false;
            }
        }
    }

    /**
     * Clear the en passant flags from the board
     */
    private void clearEnPassant() {
        for (Square[] row : squares) {
            for (Square square : row) {
                square.enPassant = false;
            }
        }
    }

    /**
     * Get whether a side is in check
     * @param side The side to check
     * @return Whether the side is in check
     */
    public boolean computeChecks(boolean side, Util.Tuple<Integer, Integer> pos) {
        int backward = side ? -1 : 1;

        // Check for pawn attacks
        if (getPieceAt(isOnBoardOrDefault(new Util.Tuple<>(pos.x() + 1, pos.y() - backward), pos)) == (side ? 4 : 2) ||
            getPieceAt(isOnBoardOrDefault(new Util.Tuple<>(pos.x() - 1, pos.y() - backward), pos)) == (side ? 4 : 2)) {
            return true;
        }

        // Check for knight attacks
        for (Util.Tuple<Integer, Integer> l : new Util.Tuple[] {
            new Util.Tuple<>(2, 1),
            new Util.Tuple<>(2, -1),
            new Util.Tuple<>(-2, 1),
            new Util.Tuple<>(-2, -1),
            new Util.Tuple<>(1, 2),
            new Util.Tuple<>(-1, 2),
            new Util.Tuple<>(1, -2),
            new Util.Tuple<>(-1, -2)
        }) {
            Util.Tuple<Integer, Integer> knightPos = new Util.Tuple<>(pos.x() + l.x(), pos.y() + l.y());
            if (isOnBoard(knightPos) && getPieceAt(knightPos) == (side ? 22 : 11)) {
                return true;
            }
        }

        // Check for bishop/queen attacks
        for (Util.Tuple<Integer, Integer> direction : new Util.Tuple[] {
                new Util.Tuple<>(1, 1),
                new Util.Tuple<>(1, -1),
                new Util.Tuple<>(-1, 1),
                new Util.Tuple<>(-1, -1)
        }) {
            for (int i = 1; i < 8; i++) {
                Util.Tuple<Integer, Integer> bishopPos = new Util.Tuple<>(pos.x() + direction.x() * i, pos.y() + direction.y() * i);
                if (!isOnBoard(bishopPos)) break;
                int piece = getPieceAt(bishopPos);
                if (piece == (side ? 14 : 7) || piece == (side ? 10 : 5)) return true;
                if (piece != -1) break;
            }
        }
        
        // Check for rook/queen attacks
        for (Util.Tuple<Integer, Integer> direction : new Util.Tuple[] {
                new Util.Tuple<>(1, 0),
                new Util.Tuple<>(-1, 0),
                new Util.Tuple<>(0, 1),
                new Util.Tuple<>(0, -1)
        }) {
            for (int i = 1; i < 8; i++) {
                Util.Tuple<Integer, Integer> rookPos = new Util.Tuple<>(pos.x() + direction.x() * i, pos.y() + direction.y() * i);
                if (!isOnBoard(rookPos)) break;
                int piece = getPieceAt(rookPos);
                if (piece == (side ? 26 : 13) || piece == (side ? 10 : 5)) return true;
                if (piece != -1) break;
            }
        }

        // Check for king attacks (not possible for a check but needed for legal move calculation)
        for (Util.Tuple<Integer, Integer> direction : new Util.Tuple[] {
             new Util.Tuple<>(-1, -1), new Util.Tuple<>(0, -1), new Util.Tuple<>(1, -1),
             new Util.Tuple<>(-1, 0), new Util.Tuple<>(1, 0),
             new Util.Tuple<>(-1, 1), new Util.Tuple<>(0, 1), new Util.Tuple<>(1, 1)
        }) {
            Util.Tuple<Integer, Integer> kingPos = new Util.Tuple<>(pos.x() + direction.x(), pos.y() + direction.y());
            if (isOnBoard(kingPos) && getPieceAt(kingPos) == (side ? 6 : 3)) {
                return true;
            }
        }


        return false;
    }

    /**
     * Reset the board to the starting position
     */
    public void resetBoard() {
        int[][] pieces = new int[][] {
            {26,22,14,10, 6,14,22,26},
            { 4, 4, 4, 4, 4, 4, 4, 4},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,-1,-1,-1},
            { 2, 2, 2, 2, 2, 2, 2, 2},
            {13,11, 7, 5, 3, 7,11,13}
        };

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                squares[i][j].setPiece(pieces[i][j]);
            }
        }

        result = 0;
        selectedPiece = null;
        whiteTurn = true;
        turnIsInCheck = false;
        kings.put(true, getSquareAt(new Util.Tuple<>(5, 1)));
        kings.put(false, getSquareAt(new Util.Tuple<>(5, 8)));
        kingsideCastling.put(true, true);
        kingsideCastling.put(false, true);
        queensideCastling.put(true, true);
        queensideCastling.put(false, true);
        past.clear();
        future.clear();
    }

    /**
     * Get the piece at a given position
     * @param pos The position to get the piece at
     * @return The piece type at the given position
     */
    public int getPieceAt(Util.Tuple<Integer, Integer> pos) {
        return getSquareAt(pos).piece;
    }

    /**
     * Get the square at a given position
     * @param pos The position to get the square at
     * @return The square at the given position
     */
    public Square getSquareAt(Util.Tuple<Integer, Integer> pos) {
        return squares[8 - pos.y()][pos.x() - 1];
    }

    /**
     * Clear all the dots from the board
     */
    public void clearDots() {
        for (Square[] row : squares) {
            for (Square square : row) {
                square.setDot(false);
            }
        }
    }

    /**
     * Check if the given position is on the board
     * @param pos The position
     * @return Whether the position is on the board
     */
    public static boolean isOnBoard(Util.Tuple<Integer, Integer> pos) {
        return pos.x() > 0 && pos.x() < 9 && pos.y() > 0 && pos.y() < 9;
    }

    /**
     * Check if the given position is on the board, and if not, return the default position
     * @param pos The position
     * @param def The default position
     * @return The position if it is on the board, or the default position if it is not
     */
    public static Util.Tuple<Integer, Integer> isOnBoardOrDefault(Util.Tuple<Integer, Integer> pos, Util.Tuple<Integer, Integer> def) {
        return isOnBoard(pos) ? pos : def;
    }

    // Extra MouseListener methods
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    public String getMoveRepresentation(Util.Tuple<Integer, Integer> from, Util.Tuple<Integer, Integer> to, boolean kingsideCastle, boolean queensideCastle, boolean enPassant) {
        StringBuilder sb = new StringBuilder();

        // Add the piece type
        sb.append(getPieceAt(from));
        sb.append("|");
        sb.append(from.x());
        sb.append(from.y());

        sb.append("m");

        // Add the move
        sb.append(getPieceAt(to));
        sb.append("|");
        sb.append(to.x());
        sb.append(to.y());

        // Add the castling
        if (kingsideCastle) sb.append("k");
        if (queensideCastle) sb.append("q");
        if (enPassant) sb.append("e");

        return sb.toString();
    }

    public void movePast() {
        if (promoting) return;

        if (past.size() == 0) return;

        clearDots();
        selectedPiece = null;

        String moveStr = past.get(past.size() - 1);

        String[] parts = moveStr.split("m");
        String[] to = parts[1].split("\\|");
        String[] toPos = to[1].split("");
        String[] from = parts[0].split("\\|");
        String[] fromPos = from[1].split("");

        getSquareAt(new Util.Tuple<>(Integer.parseInt(toPos[0]), Integer.parseInt(toPos[1]))).setPiece(Integer.parseInt(to[0]));
        getSquareAt(new Util.Tuple<>(Integer.parseInt(fromPos[0]), Integer.parseInt(fromPos[1]))).setPiece(Integer.parseInt(from[0]));

        whiteTurn = !whiteTurn;

        if (Integer.parseInt(from[0]) == (whiteTurn ? 3 : 6)) {
            kings.put(whiteTurn, getSquareAt(new Util.Tuple<>(Integer.parseInt(fromPos[0]), Integer.parseInt(fromPos[1]))));

            if (Math.abs(Integer.parseInt(toPos[0]) - Integer.parseInt(fromPos[0])) == 2) {
                if (Integer.parseInt(toPos[0]) > Integer.parseInt(fromPos[0])) {
                    getSquareAt(new Util.Tuple<>(6, whiteTurn ? 1 : 8)).setPiece(-1);
                    getSquareAt(new Util.Tuple<>(8, whiteTurn ? 1 : 8)).setPiece(whiteTurn ? 13 : 26);
                } else {
                    getSquareAt(new Util.Tuple<>(4, whiteTurn ? 1 : 8)).setPiece(-1);
                    getSquareAt(new Util.Tuple<>(1, whiteTurn ? 1 : 8)).setPiece(whiteTurn ? 13 : 26);
                }
            }
        }

        if (to[1].contains("k")) {
            kingsideCastling.put(whiteTurn, true);
        } else {
            kingsideCastling.put(whiteTurn, false);
        }

        if (to[1].contains("q")) {
            queensideCastling.put(whiteTurn, true);
        } else {
            queensideCastling.put(whiteTurn, false);
        }

        if (to[1].contains("e")) {
            getSquareAt(new Util.Tuple<>(Integer.parseInt(toPos[0]), Integer.parseInt(fromPos[1]))).setPiece(whiteTurn ? 4 : 2);
            getSquareAt(new Util.Tuple<>(Integer.parseInt(toPos[0]), Integer.parseInt(toPos[1]))).enPassant = true;
        }

        endTurn();
        repaint();

        past.remove(past.size() - 1);
        future.add(moveStr);
    }

    public void moveFuture() {
        if (promoting) return;

        if (future.size() == 0) return;

        clearDots();
        selectedPiece = null;

        String moveStr = future.get(future.size() - 1);

        String[] parts = moveStr.split("m");
        String[] to = parts[1].split("\\|");
        String[] toPos = to[1].split("");
        String[] from = parts[0].split("\\|");
        String[] fromPos = from[1].split("");

        getSquareAt(new Util.Tuple<>(Integer.parseInt(toPos[0]), Integer.parseInt(toPos[1]))).setPiece(Integer.parseInt(from[0]));
        getSquareAt(new Util.Tuple<>(Integer.parseInt(fromPos[0]), Integer.parseInt(fromPos[1]))).setPiece(-1);

        if (Integer.parseInt(from[0]) == (whiteTurn ? 3 : 6)) {
            kings.put(whiteTurn, getSquareAt(new Util.Tuple<>(Integer.parseInt(toPos[0]), Integer.parseInt(toPos[1]))));

            if (Math.abs(Integer.parseInt(toPos[0]) - Integer.parseInt(fromPos[0])) == 2) {
                if (Integer.parseInt(toPos[0]) > Integer.parseInt(fromPos[0])) {
                    getSquareAt(new Util.Tuple<>(8, whiteTurn ? 1 : 8)).setPiece(-1);
                    getSquareAt(new Util.Tuple<>(6, whiteTurn ? 1 : 8)).setPiece(whiteTurn ? 13 : 26);
                } else {
                    getSquareAt(new Util.Tuple<>(1, whiteTurn ? 1 : 8)).setPiece(-1);
                    getSquareAt(new Util.Tuple<>(4, whiteTurn ? 1 : 8)).setPiece(whiteTurn ? 13 : 26);
                }
            }
        }

        if (to[1].contains("k")) {
            kingsideCastling.put(whiteTurn, true);
        } else {
            kingsideCastling.put(whiteTurn, false);
        }

        if (to[1].contains("q")) {
            queensideCastling.put(whiteTurn, true);
        } else {
            queensideCastling.put(whiteTurn, false);
        }

        if (to[1].contains("e")) {
            getSquareAt(new Util.Tuple<>(Integer.parseInt(toPos[0]), Integer.parseInt(fromPos[1]))).setPiece(-1);
        }

        if (to[1].contains("p")) {
            getSquareAt(new Util.Tuple<>(Integer.parseInt(toPos[0]), Integer.parseInt(toPos[1]))).setPiece(Integer.parseInt(String.valueOf(to[1].charAt(to[1].length()-1))));
        }

        whiteTurn = !whiteTurn;

        endTurn();
        repaint();

        past.add(moveStr);
        future.remove(future.size() - 1);
    }

    public void save() {
        save(new File(MainScreen.savePath, "kchess.sav"));
        save(new File(destinationFile));
    }

    public void save(File fileName) {
        // Create a save file called fileName and write the past and future moves to it separated by a hyphen
        try {
            //noinspection ResultOfMethodCallIgnored
            fileName.createNewFile();
            FileWriter saveFile = new FileWriter(fileName);

            saveFile.write(player1 + "\n");
            saveFile.write(player2 + "\n");
            saveFile.write(destinationFile + "\n");

            saveFile.write("---\n");

            for (Square[] row : squares) {
                for (Square square : row) {
                    saveFile.write(square.piece + " ");
                }
                saveFile.write("\n");
            }

            saveFile.write((whiteTurn ? "1" : "0") + "\n");
            saveFile.write((kingsideCastling.get(true) ? "1" : "0") + " " + (queensideCastling.get(true) ? "1" : "0") + "\n");
            saveFile.write((kingsideCastling.get(false) ? "1" : "0") + " " + (queensideCastling.get(false) ? "1" : "0") + "\n");

            saveFile.write("---\n");

            saveFile.write(String.join("\n", past) + "\n---\n" + String.join("\n", future) + "\n---");
            saveFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Board load(File fileName, Dimension rootSize) {
        // Load a save file called fileName defined as per above and load values from it
        try {
            Scanner saveFile = new Scanner(fileName);

            Board board = new Board(
                rootSize,
                saveFile.nextLine(),
                saveFile.nextLine()
            );

            board.destinationFile = saveFile.nextLine();

            saveFile.nextLine();

            for (Square[] row : board.squares) {
                for (Square square : row) {
                    square.setPiece(Integer.parseInt(saveFile.next()));
                }
            }

            board.whiteTurn = saveFile.next().equals("1");
            board.kingsideCastling.put(true, saveFile.next().charAt(0) == '1');
            board.queensideCastling.put(true, saveFile.next().charAt(0) == '1');
            board.kingsideCastling.put(false, saveFile.next().charAt(0) == '1');
            board.queensideCastling.put(false, saveFile.next().charAt(0) == '1');

            saveFile.nextLine();
            saveFile.nextLine();

            String next;
            while (!(next = saveFile.nextLine()).equals("---")) {
                if (!next.isEmpty())
                    board.past.add(next);
            }

            while (!(next = saveFile.nextLine()).equals("---")) {
                if (!next.isEmpty())
                    board.future.add(next);
            }
            return board;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("File not found");
        }
    }
}
