package uk.co.anshroid.kchess;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a square on the board
 */

@SuppressWarnings({"unchecked"})
public class Square extends JPanel {
    public static final HashMap<Integer, Image> sprites = new HashMap<>();
    public static final HashMap<Integer, String> pieceChars = new HashMap<Integer, String>() {{
        put(-1, " ");
        put(2, "♙");
        put(4, "♟");
        put(3, "♔");
        put(6, "♚");
        put(5, "♕");
        put(10, "♛");
        put(7, "♗");
        put(14, "♝");
        put(11, "♘");
        put(22, "♞");
        put(13, "♖");
        put(26, "♜");
    }};

    public JLabel pieceLabel = new JLabel();

    final Board board;
    public final Util.Tuple<Integer, Integer> pos;

    public boolean dot = false;
    public boolean check = false;
    public static final int dotSize = 15;

    public int piece;
    public boolean enPassant;

    /**
     * Create a new Square object
     *
     * @param pos    The position of the square
     * @param parent The parent board
     */
    public Square(Util.Tuple<Integer, Integer> pos, Board parent) {
        super();
        loadSprites();

        this.board = parent;
        this.pos = pos;
        setBackground((pos.x() + pos.y()) % 2 == 1 ? Color.WHITE : Color.GRAY);

        if (Util.isKindle()) {
            pieceLabel = new JLabel(pieceChars.get(piece));
            pieceLabel.setAlignmentX(0.5f);
            pieceLabel.setAlignmentY(0.5f);

            // From https://stackoverflow.com/questions/12485236/finding-maximum-font-size-to-fit-in-region-java
            Font base;
            base = Font.getFont("FreeSerif");

            if (base == null) {
                base = new Font("Courier", Font.PLAIN, 1);
            }

            Font f = base.deriveFont(Font.PLAIN, 1);
            FontMetrics fm;
            do {
                f = base.deriveFont(Font.PLAIN, f.getSize() + 1);
                fm = this.getFontMetrics(f);
            } while (fm.stringWidth("♚") < (Board.rootSize.width / 8) && fm.getHeight() < (Board.rootSize.width / 8));

            pieceLabel.setFont(f);
            add(pieceLabel);
        }
    }

    /**
     * Draw the square graphics
     *
     * @param g the <code>Graphics</code> object to use
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (piece != -1) {
            g.drawImage(sprites.get(piece), 0, 0, this);
        }

        if (dot) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillOval((getWidth() - dotSize) / 2, (getHeight() - dotSize) / 2, dotSize, dotSize);
        }

        if (check) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillOval((getWidth() - dotSize) / 2, (getHeight() - dotSize) / 2, dotSize, dotSize);
        }
    }

    /**
     * Set whether a dot is present on the square
     *
     * @param dot Whether a dot is present
     */
    public void setDot(boolean dot) {
        this.dot = dot;
        repaint();
    }

    /**
     * Set whether the square is in check
     *
     * @param check Whether the square is in check
     */
    public void setCheck(boolean check) {
        this.check = check;
        repaint();
    }

    /**
     * Set the piece on the square
     *
     * @param piece The piece to set
     */
    public void setPiece(int piece) {
        this.piece = piece;
        if (Util.isKindle()) {
            pieceLabel.setText(pieceChars.get(piece));
        }
        repaint();
    }

    /**
     * Get all legal moves for the piece on this square
     *
     * @return An array of legal moves
     */
    public ArrayList<Util.Tuple<Integer, Integer>> getLegalMoves() {
        ArrayList<Util.Tuple<Integer, Integer>> moves = new ArrayList<>();
        Util.Tuple<Integer, Integer> move;

        switch (piece) {
            case 2: // Pawn
            case 4:
                int forward = isWhite() ? 1 : -1; // The direction the pawn moves in
                move = new Util.Tuple<>(pos.x(), pos.y() + forward); // Check 1s^
                if (Board.isOnBoard(move) && board.getPieceAt(move) == -1) { // Check the target is on the board and empty
                    moves.add(move); // Add the move

                    move = new Util.Tuple<>(pos.x(), pos.y() + (2 * forward)); // Check 2s^
                    if (pos.y() == (isWhite() ? 2 : 7) && board.getPieceAt(move) == -1) { // Check the pawn is in the correct position and the target is empty
                        moves.add(move); // Add the move
                    }
                }
                move = new Util.Tuple<>(pos.x() + 1, pos.y() + forward); // Check takes to the right
                // Check the target is on the board and contains an enemy piece
                if (Board.isOnBoard(move) && board.getPieceAt(move) != -1 && (isWhite(board.getPieceAt(move)) != isWhite())) {
                    moves.add(move); // Add the move
                }
                move = new Util.Tuple<>(pos.x() - 1, pos.y() + forward); // Check takes to the left
                // Check the target is on the board and contains an enemy piece
                if (Board.isOnBoard(move) && board.getPieceAt(move) != -1 && (isWhite(board.getPieceAt(move)) != isWhite())) {
                    moves.add(move);
                }

                move = new Util.Tuple<>(pos.x() + 1, pos.y() + forward); // Check en passant to the right
                // Check the target is on the board and contains an enemy pawn
                if (Board.isOnBoard(move) && board.getPieceAt(new Util.Tuple<>(pos.x() + 1, pos.y())) == (isWhite() ? 4 : 2) && board.getSquareAt(move).enPassant) {
                    moves.add(move); // Add the move
                }

                move = new Util.Tuple<>(pos.x() - 1, pos.y() + forward); // Check en passant to the left
                // Check the target is on the board and contains an enemy pawn
                if (Board.isOnBoard(move) && board.getPieceAt(new Util.Tuple<>(pos.x() - 1, pos.y())) == (isWhite() ? 4 : 2) && board.getSquareAt(move).enPassant) {
                    moves.add(move); // Add the move
                }
                break;
            case 3: // King
            case 6:
                for (Util.Tuple<Integer, Integer> offset : new Util.Tuple[]{
                        new Util.Tuple<>(-1, -1), new Util.Tuple<>(0, -1), new Util.Tuple<>(1, -1),
                        new Util.Tuple<>(-1, 0), new Util.Tuple<>(1, 0),
                        new Util.Tuple<>(-1, 1), new Util.Tuple<>(0, 1), new Util.Tuple<>(1, 1)
                }) {
                    offset = new Util.Tuple<>(pos.x() + offset.x(), pos.y() + offset.y()); // Add the offset to the current position
                    if (Board.isOnBoard(offset) && (board.getPieceAt(offset) == -1 || isWhite(board.getPieceAt(offset)) != isWhite())) { // Check the target is on the board and either empty or contains an enemy piece
                        moves.add(offset); // Add the move
                    }
                }

                if (!check) {
                    if (board.kingsideCastling.get(isWhite())) {
                        boolean obstructed = false;
                        for (Util.Tuple<Integer, Integer> obstruction : new Util.Tuple[]{
                                new Util.Tuple<>(pos.x() + 1, pos.y()),
                                new Util.Tuple<>(pos.x() + 2, pos.y())
                        }) {
                            if (board.getPieceAt(obstruction) != -1 || board.computeChecks(isWhite(), obstruction)) {
                                obstructed = true;
                            }
                        }

                        if (!obstructed) {
                            moves.add(new Util.Tuple<>(pos.x() + 2, pos.y()));
                        }
                    }

                    if (board.queensideCastling.get(isWhite())) {
                        boolean obstructed = false;
                        for (Util.Tuple<Integer, Integer> obstruction : new Util.Tuple[]{
                                new Util.Tuple<>(pos.x() - 1, pos.y()),
                                new Util.Tuple<>(pos.x() - 2, pos.y())
                        }) {
                            if (board.getPieceAt(obstruction) != -1 || board.computeChecks(isWhite(), obstruction)) {
                                obstructed = true;
                            }
                        }

                        if (!obstructed) {
                            moves.add(new Util.Tuple<>(pos.x() - 2, pos.y()));
                        }
                    }
                }
                break;
            case 5: // Queen
            case 10:
                for (Util.Tuple<Integer, Integer> offset : new Util.Tuple[]{
                        new Util.Tuple<>(-1, -1), new Util.Tuple<>(0, -1), new Util.Tuple<>(1, -1),
                        new Util.Tuple<>(-1, 0), new Util.Tuple<>(1, 0),
                        new Util.Tuple<>(-1, 1), new Util.Tuple<>(0, 1), new Util.Tuple<>(1, 1)
                }) {
                    CheckDirection(moves, offset);
                }
                break;
            case 7: // Bishop
            case 14:
                for (Util.Tuple<Integer, Integer> offset : new Util.Tuple[]{
                        new Util.Tuple<>(-1, -1), new Util.Tuple<>(1, -1),
                        new Util.Tuple<>(-1, 1), new Util.Tuple<>(1, 1)
                }) {
                    CheckDirection(moves, offset);
                }
                break;
            case 11: // Knight
            case 22:
                for (Util.Tuple<Integer, Integer> l : new Util.Tuple[]{
                        new Util.Tuple<>(2, 1),
                        new Util.Tuple<>(2, -1),
                        new Util.Tuple<>(-2, 1),
                        new Util.Tuple<>(-2, -1),
                        new Util.Tuple<>(1, 2),
                        new Util.Tuple<>(-1, 2),
                        new Util.Tuple<>(1, -2),
                        new Util.Tuple<>(-1, -2)
                }) {
                    move = new Util.Tuple<>(pos.x() + l.x(), pos.y() + l.y());
                    if (Board.isOnBoard(move) && (board.getPieceAt(move) == -1 || isWhite(board.getPieceAt(move)) != isWhite())) {
                        moves.add(move);
                    }
                }
                break;
            case 13: // Rook
            case 26:
                for (Util.Tuple<Integer, Integer> offset : new Util.Tuple[]{
                        new Util.Tuple<>(1, 0),
                        new Util.Tuple<>(-1, 0),
                        new Util.Tuple<>(0, 1),
                        new Util.Tuple<>(0, -1)
                }) {
                    CheckDirection(moves, offset);
                }
                break;
        }

        // Check each move for check-legality
        for (Util.Tuple<Integer, Integer> move1 : (ArrayList<Util.Tuple<Integer, Integer>>) moves.clone()) {
            int to = board.getPieceAt(move1); // Temporarily store the piece at the target square
            int from = piece; // Temporarily store the piece on this square

            board.getSquareAt(move1).piece = piece; // Temporarily move the piece
            if (piece == 3 || piece == 6) {
                board.kings.put(isWhite(piece), board.getSquareAt(move1));
            }
            piece = -1;

            if (board.computeChecks(isWhite(from), board.kings.get(isWhite(from)).pos)) { // Check if the move puts the player in check
                moves.remove(move1); // Remove the move if it does
            }

            board.getSquareAt(move1).piece = to;
            piece = from; // Undo the temporary move
            if (piece == 3 || piece == 6) {
                board.kings.put(isWhite(piece), this);
            }
        }
        repaint();
        return moves;
    }

    private void CheckDirection(ArrayList<Util.Tuple<Integer, Integer>> moves, Util.Tuple<Integer, Integer> offset) {
        Util.Tuple<Integer, Integer> move;
        for (int i = 1; i < 8; i++) {
            move = new Util.Tuple<>(pos.x() + (offset.x() * i), pos.y() + (offset.y() * i)); // Add the offset to the current position
            if (Board.isOnBoard(move) && (board.getPieceAt(move) == -1 || isWhite(board.getPieceAt(move)) != isWhite())) { // Check the target is on the board and either empty or contains an enemy piece
                moves.add(move); // Add the move
                if (board.getPieceAt(move) != -1) { // Check if the target is occupied
                    break; // Stop checking in this direction if it is
                }
            } else {
                break; // Stop checking in this direction if the target is off the board
            }
        }
    }

    /**
     * Check if the piece on this square is white
     *
     * @return Whether the piece is white
     */
    public boolean isWhite() {
        return isWhite(piece);
    }

    /**
     * Check if the given piece is white
     *
     * @param type The piece type
     * @return Whether the piece is white
     */
    public static boolean isWhite(int type) {
        return type == 2 || type % 2 == 1;
    }

    /**
     * Load all sprites into the sprites map
     */
    public static void loadSprites() {
        if (!sprites.isEmpty()) {
            return; // Don't load more than once
        }

        if (Util.isKindle()) {
            return; // Can't use image sprites on Kindle
        }

        int size = Board.rootSize.width / 8; // Piece size
        for (int i : new int[]{2, 3, 4, 5, 6, 7, 10, 11, 13, 14, 22, 26}) {
            try {
//                 Load the sprite and scale it to the correct size (only works on desktop)
                sprites.put(i, ImageIO.read(Objects.requireNonNull(Square.class.getResource("/" + i + ".png"))).getScaledInstance(size, size, Image.SCALE_SMOOTH));

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to load sprite " + i);
            }
        }
    }
}