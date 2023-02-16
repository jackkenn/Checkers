import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Game g = new Game();
        Alphabetaa red = new Alphabetaa(g, 1, 5, b -> b.redCount - b.blackCount);
        Alphabetaa black = new Alphabetaa(g, -1, 5, b -> b.blackCount - b.redCount);
        int aih1 = 0;
        int aid1 = 0;
        int aih2 = 0;
        int aid2 = 0;
        Scanner scan = new Scanner(System.in);
        System.out.println("Number of Ai(0-2): ");
        while (!scan.hasNextInt()) {}
        int num = scan.nextInt();
        if(num >=1) {
            System.out.println("Reds heuristic(1-3): ");
            while (!scan.hasNextInt()) {}
            aih1 = scan.nextInt();
            System.out.println("Reds depth(1-9): ");
            while (!scan.hasNextInt()) {}
            aid1 = scan.nextInt();
            if(aih1==2) {
                red = new Alphabetaa(g, 1, aid1, b -> {
                    int h = 0;
                    for(Piece p : b.grid) {
                        h += p.value;
                    }
                    return h;
                });
            } else if(aih1==3) {
                red = new Alphabetaa(g, 1, 8, b -> {
                    int h = 0;
                    for(int i=0; i<32; i++) {
                        Piece p = b.grid[i];
                        h += p.value2;
                        if(!p.isKing) {
                            h += p.player>0? i%4: i%4-12;
                        }
                    }
                    return h;
                });
            } else {
                red = new Alphabetaa(g, 1, aid1, b -> b.redCount - b.blackCount);
            }
        }
        if(num >1) {
            System.out.println("Black heuristic(1-3): ");
            while (!scan.hasNextInt()) {}
            aih2 = scan.nextInt();
            System.out.println("Black depth(1-9): ");
            while (!scan.hasNextInt()) {}
            aid2 = scan.nextInt();
            if(aih2==2) {
                black = new Alphabetaa(g, -1, aid2, b -> {
                    int h = 0;
                    for(Piece p : b.grid) {
                        h -= p.value;
                    }
                    return h;
                });
            } else if(aih2==3) {
                black = new Alphabetaa(g, -1, aid2, b -> {
                    int h = 0;
                    for(int i=0; i<32; i++) {
                        Piece p = b.grid[i];
                        h -= p.value2;
                        if(!p.isKing) {
                            h -= p.player>0? i%4: i%4-12;
                        }
                    }
                    return h;
                });
            } else {
                black = new Alphabetaa(g, -1, 5, b -> b.blackCount - b.redCount);
            }
        }
        int i = 0;
        System.out.println("Game Start\n" + g.board);
        System.out.println(g.getMoves());
        scan = new Scanner(System.in);
        boolean end = false;
        while (!end && i < 500 && g.legalMoves.size() > 0) {
            if(num<2) {
                while (!scan.hasNextLine()) {}
                String s = scan.nextLine().replaceAll(",", "");
                if(s.toUpperCase().contains("STOP")) {
                    end = true;
                } else {
                    g.move(new Move(s));
                }
            }
            if(num >= 2) {
                if(g.board.blackCount <= 1 || g.board.redCount <= 1) {
                    break;
                }
                if (!(i%2==0?g.move(black.search()):g.move(red.search()))) {
                    System.out.println("Move ERROR");
                    i += 100;
                }
            } else if(num == 1) {
                if(!g.move(red.search())) {
                    System.out.println("Move ERROR");
                    i += 100;
                }
            }
            i++;
        }
        scan.close();
        System.out.println("Moves played: " + g.moveList);
        System.out.println("Black pieceCount: " + g.board.blackCount + "\nRed pieceCount: " + g.board.redCount);
        if(g.legalMoves.size() < 1) {
            System.out.println((i%2==0? "Black": "RED") + " is Out of Moves");
        } else if(i >= 500){
            System.out.println("Move limit reached");
        }
    }

    private static class Board {
        public Piece grid[];
        public int redCount;
        public int blackCount;
        public Move prevMove;
        public int turn;

        public Board() {
            grid = new Piece[] {
                    Piece.RED__, Piece.RED__, Piece.RED__, Piece.RED__,
                    Piece.RED__, Piece.RED__, Piece.RED__, Piece.RED__,
                    Piece.RED__, Piece.RED__, Piece.RED__, Piece.RED__,
                    Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY,
                    Piece.EMPTY, Piece.EMPTY, Piece.EMPTY, Piece.EMPTY,
                    Piece.BLACK, Piece.BLACK, Piece.BLACK, Piece.BLACK,
                    Piece.BLACK, Piece.BLACK, Piece.BLACK, Piece.BLACK,
                    Piece.BLACK, Piece.BLACK, Piece.BLACK, Piece.BLACK
            };
            turn = -1;
            redCount = 12;
            blackCount = 12;
        }

        public Board(Board b) {
            grid = b.grid.clone();
            prevMove = b.prevMove;
            turn = b.turn;
            redCount = b.redCount;
            blackCount = b.blackCount;
        }

        @Override
        public String toString() {
            String s = new String();
            for(int i = 0; i < 32; i++) {
                if(i%4==0) {
                    s += "\n";
                }
                if(i%8==0) {
                    s += "  ";
                }
                s += grid[i] + "" + (i < 10 ? "0"+ i : i);
            }
            return s;
        }

        public String asKey() {
            String s = new String();
            s+= turn + "";
            for(Piece p : grid) {
                s+=p.toString();
            }
            return s;
        }

        public Board move(Move m) {
            var b = new Board(this);
            b.prevMove = m;
            b.turn *=-1;
            var tmp = b.grid[m.nextIndex];
            b.grid[m.nextIndex] = b.grid[m.prevIndex].kingMe(m.nextIndex);
            b.grid[m.prevIndex] = tmp;
            return b;
        }
    }

    private static enum Direction {
        UL(-4, -5, -9) {
            @Override
            public List<Move> calcMoves(Board b, int index, List<Move> moves) {
                int mi = index%8;
                if(mi!=4) {
                    int offset = mi<4 ? index+value : index+slideValue;
                    if(offset >= 0 && b.grid[offset] == Piece.EMPTY) {
                        moves.add(new Move(index, offset));
                    }
                }
                return moves;
            }

            @Override
            public List<Move> calcJumps(Board b, int index, List<Move> moves, int opp) {
                int mi = index%8;
                if(mi!=4&&mi!=0) {
                    int offset = mi<4 ? index+value : index+slideValue;
                    int jumInt = index+jump;
                    if(jumInt >= 0 && b.grid[offset].player == opp && b.grid[jumInt] == Piece.EMPTY) {
                        moves.add(new Move(index, jumInt, offset));
                    }
                }
                return moves;
            }
        },
        UR(-3, -4, -7) {
            @Override
            public List<Move> calcMoves(Board b, int index, List<Move> moves) {
                int mi = index%8;
                if(mi!=3) {
                    int offset = mi<4 ? index+value : index+slideValue;
                    if(offset >= 0 && b.grid[offset] == Piece.EMPTY) {
                        moves.add(new Move(index, offset));
                    }
                }
                return moves;
            }

            @Override
            public List<Move> calcJumps(Board b, int index, List<Move> moves, int opp) {
                int mi = index%8;
                if(mi!=3&&mi!=7) {
                    int offset = mi<4 ? index+value : index+slideValue;
                    int jumInt = index+jump;
                    if(jumInt >= 0 && b.grid[offset].player == opp && b.grid[jumInt] == Piece.EMPTY) {
                        moves.add(new Move(index, jumInt, offset));
                    }
                }
                return moves;
            }
        },
        DL(4, 3, 7) {
            @Override
            public List<Move> calcMoves(Board b, int index, List<Move> moves) {
                int mi = index%8;
                if(mi!=4) {
                    int offset = mi<4 ? index+value : index+slideValue;
                    if(offset < 32 && b.grid[offset] == Piece.EMPTY) {
                        moves.add(new Move(index, offset));
                    }
                }
                return moves;
            }

            @Override
            public List<Move> calcJumps(Board b, int index, List<Move> moves, int opp) {
                int mi = index%8;
                if(mi!=4&&mi!=0) {
                    int offset = mi<4 ? index+value : index+slideValue;
                    int jumInt = index+jump;
                    if(jumInt < 32 && b.grid[offset].player == opp && b.grid[jumInt] == Piece.EMPTY) {
                        moves.add(new Move(index, jumInt, offset));
                    }
                }
                return moves;
            }
        },
        DR(5, 4, 9) {
            @Override
            public List<Move> calcMoves(Board b, int index, List<Move> moves) {
                int mi = index%8;
                if(mi!=3) {
                    int offset = mi<4 ? index+value : index+slideValue;
                    if(offset < 32 && b.grid[offset] == Piece.EMPTY) {
                        moves.add(new Move(index, offset));
                    }
                }
                return moves;
            }

            @Override
            public List<Move> calcJumps(Board b, int index, List<Move> moves, int opp) {
                int mi = index%8;
                if(mi!=3&&mi!=7) {
                    int offset = mi<4 ? index+value : index+slideValue;
                    int jumInt = index+jump;
                    if(jumInt < 32 && b.grid[offset].player == opp && b.grid[jumInt] == Piece.EMPTY) {
                        moves.add(new Move(index, jumInt, offset));
                    }
                }
                return moves;
            }
        };

        protected int value = 0;
        protected int slideValue = 0;
        protected int jump = 0;

        Direction(int value, int slideValue, int jump) {
            this.value = value;
            this.slideValue = slideValue;
            this.jump = jump;
        }

        public List<Move> calcMoves(Board b, int index, List<Move> moves) {
            return null;
        }

        public List<Move> calcJumps(Board b, int index, List<Move> moves, int opp) {
            return null;
        }
    }

    private static class Move {
        public int prevIndex;
        public int nextIndex;
        public int jumped = -1;

        public Move(int prevIndex, int nextIndex) {
            this.prevIndex = prevIndex;
            this.nextIndex = nextIndex;
        }

        public Move(int prevIndex, int nextIndex, int jumped) {
            this.prevIndex = prevIndex;
            this.nextIndex = nextIndex;
            this.jumped = jumped;
        }

        public Move(String s) {
            Scanner scan = new Scanner(s);
            prevIndex = scan.nextInt();
            nextIndex = scan.nextInt();
            scan.close();
        }

        private int strToInt(String s) throws Exception {
            int num = 0;
            switch(s.toUpperCase().toCharArray()[0]) {
                case 'A':
                    num = 0;
                    break;
                case 'B':
                    num = 0;
                    break;
                case 'C':
                    num = 1;
                    break;
                case 'D':
                    num = 1;
                    break;
                case 'E':
                    num = 2;
                    break;
                case 'F':
                    num = 2;
                    break;
                case 'H':
                    num = 3;
                    break;
                default:
                    throw new Exception("spot not found");
            }
            return num + (Integer.parseInt(s.toCharArray()[1] + "") - 1) * 4;
        }

        @Override
        public String toString() {
            return prevIndex + " " + nextIndex;
        }
    }

    private static class Alphabetaa {
        private Function<Board, Integer> h;
        private Game game;
        private int player;
        private int maxDepth;

        public Alphabetaa(Game game, int player, int maxDepth, Function<Board, Integer> h) {
            this.h = h;
            this.game = game;
            this.player = player;
            this.maxDepth = maxDepth;
        }

        public Move search() {
            if(game.board.turn != player) {
                System.out.println("Wrong Turn");
                return null;
            }
            long deltaTime = System.nanoTime();
            int best = Integer.MIN_VALUE;
            Move startMove = null;
            for(Board b : Game.genMoves(game.board)) {
                int val = h.apply(b);
                Node n = alpha(0,  new Node(b, val), Integer.MIN_VALUE, Integer.MAX_VALUE);
                if(best <= n.value) {
                    startMove = b.prevMove;
                    best = n.value;
                }
            }
            System.out.println("Move made: " + startMove + "\nTime Taken(ns): " + (System.nanoTime()-deltaTime));
            return startMove;
        }

        private Node alpha(int depth, Node node, int alpha, int beta) {
            if(depth>maxDepth) {
                return node;
            }
            Node best = new Node(node.board, Integer.MIN_VALUE);
            for(Board b : Game.genMoves(node.board)) {
                Node n = new Node(b, h.apply(node.board));
                n = beta(depth+1, n, alpha, beta);
                best = n.value < best.value ? best : n;
                alpha = best.value < alpha ? alpha : best.value;
                if(beta <= alpha) {
                    break;
                }
            }
            return best;
        }

        private Node beta(int depth, Node node, int alpha, int beta) {
            if(depth>maxDepth) {
                return node;
            }
            Node best = new Node(node.board, Integer.MAX_VALUE);
            for(Board b : Game.genMoves(node.board)) {
                Node n = new Node(b, h.apply(node.board));
                n = alpha(depth+1, n, alpha, beta);
                best = n.value > best.value ? best : n;
                beta = best.value > beta ? beta : best.value;
                if(beta <= alpha) {
                    break;
                }
            }
            return best;
        }

        private class Node{
            public Board board;
            public int value;

            public Node(Board board, int value) {
                this.board = board;
                this.value = value;
            }
        }
    }

    private static class Game {
        public Board board = new Board();
        public HashSet<Board> legalMoves;
        public String moveList = new String();

        public Game() {
            legalMoves = genMoves(board);
        }

        static public HashSet<Board> genMoves(Board b) {
            HashSet<Board> moves = new HashSet<>();
            for(int i = 0; i < 32; i++) {
                if(b.grid[i].player == b.turn) {
                    moves.addAll(b.grid[i].getMoves(b, i).stream().map(m -> b.move(m)).collect(Collectors.toSet()));
                    for(Move m: b.grid[i].getJumps(b, i)) {
                        jumpsRec(b, m, moves, i, b.turn*-1);
                    }
                }
            }
            return moves;
        }

        static private HashSet<Board> jumpsRec(Board b, Move m, HashSet<Board> moves, int origin, int nextTurn) {
            b = b.move(m);
            b.prevMove.prevIndex = origin;
            b.turn = nextTurn;
            if(b.grid[m.jumped].player==1) {
                b.redCount--;
            } else {
                b.blackCount--;
            }
            b.grid[m.jumped] = Piece.EMPTY;
            moves.add(b);
            for (Move nextMove : b.grid[m.nextIndex].getJumps(b, m.nextIndex)) {
                jumpsRec(b, nextMove, moves, origin, nextTurn);
            }
            return moves;
        }

        public boolean move(Move m) {
            if(m==null) {
                System.out.println("ERROR: move is null");
                return false;
            }
            var moveMap = new HashMap<String, Board>() {{
                for (Board b : legalMoves) {
                    if(!this.containsKey(b)) {
                        this.put(b.prevMove.toString(), b);
                    }
                }
            }};
            if(moveMap.containsKey(m.toString())) {
                board = moveMap.get(m.toString());
                endTurn();
                moveList += (moveList.length() > 0 ? ", " : "") + m;
                return true;
            }
            System.out.println("ERROR: illegal move. please format move as seen above");
            return false;
        }

        public List<Move> getMoves() {
            return new ArrayList<>() {{ legalMoves.forEach((v) -> add(v.prevMove));}};
        }

        private void endTurn() {
            legalMoves = genMoves(board);
            System.out.println(board);
            System.out.println(getMoves());
        }
    }

    private static enum Piece {
        EMPTY(new Direction[] { }, 0, 0, 0, false)  {
            @Override
            public List<Move> getMoves(Board b, int index) {
                return new ArrayList<>();
            }

            @Override
            public List<Move> getJumps(Board b, int index) {
                return new ArrayList<>();
            }

            @Override
            public String toString() {
                return " _";
            }
        },
        RED__(new Direction[] { Direction.DL, Direction.DR }, 1, 1, 24, false)  {
            @Override
            public String toString() {
                return " r";
            }

            @Override
            public Piece kingMe(int index) {
                return index>27? REDKING:this;
            }
        },
        BLACK(new Direction[] { Direction.UR, Direction.UL }, -1, -1, -24, false)  {
            @Override
            public String toString() {
                return " b";
            }

            public Piece kingMe(int index) {
                return index<4? BLACKKING:this;
            }
        },
        REDKING(new Direction[] { Direction.UR, Direction.UL, Direction.DL, Direction.DR }, 1, 2, 48, true) {
            @Override
            public String toString() {
                return " R";
            }
        },
        BLACKKING(new Direction[] { Direction.UR, Direction.UL, Direction.DL, Direction.DR }, -1, -2, -48, true) {
            @Override
            public String toString() {
                return " B";
            }
        };

        protected Direction[] dirs;
        public int player = 0;
        public int value;
        public int value2;
        public boolean isKing;

        Piece(Direction[] dirs, int player, int value, int value2, boolean isKing) {
            this.dirs = dirs;
            this.player = player;
            this.value = value;
            this.value2 = value2;
            this.isKing = isKing;
        }

        public List<Move> getMoves(Board b, int index) {
            List<Move> a = new ArrayList<>();
            for (Direction d : dirs) {
                d.calcMoves(b, index, a);
            }
            return a;
        }

        public List<Move> getJumps(Board b, int index) {
            List<Move> a = new ArrayList<>();
            for (Direction d : dirs) {
                d.calcJumps(b, index, a, this.player*-1);
            }
            return a;
        }

        public Piece kingMe(int index) {
            return this;
        }
    }
}

