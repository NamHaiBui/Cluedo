
/**
 * ClueReasoner.java - project skeleton for a propositional reasoner
 * for the game of Clue.  Unimplemented portions have the comment "TO
 * BE IMPLEMENTED AS AN EXERCISE".  The reasoner does not include
 * knowledge of how many cards each player holds.  See
 * http://cs.gettysburg.edu/~tneller/nsf/clue/ for details.
 *
 * @author Todd Neller
 * @version 1.0
 *

Copyright (C) 2005 Todd Neller

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

Information about the GNU General Public License is available online at:
  http://www.gnu.org/licenses/
To receive a copy of the GNU General Public License, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
02111-1307, USA.

 */

import java.io.*;
import java.util.*;

public class ClueReasoner {

    // numPlayers: The number of players
    private int numPlayers;
    // playerNum: the index of the player in the players array
    private int playerNum;
    // numsCards: the number of Cards
    private int numCards;
    // the SATSolver used to reason
    private SATSolver solver;
    // caseFile: the String representing the Case File
    private String caseFile = "cf";
    // players: an array of Strings representing the players in their turn order
    private String[] players = { "sc", "mu", "wh", "gr", "pe", "pl" };
    // suspects: an array of Strings representing the suspects
    private String[] suspects = { "mu", "pl", "gr", "pe", "sc", "wh" };
    // weapons: an array of Strings representing the weapons
    private String[] weapons = { "kn", "ca", "re", "ro", "pi", "wr" };
    // rooms: an array of Strings representing the rooms
    private String[] rooms = { "ha", "lo", "di", "ki", "ba", "co", "bi", "li", "st" };
    // cards: an array of Strings representing all suspects, weapons, and rooms
    private String[] cards;

    /**
     * Constructing Clue Reasoners by initializing the card categories and the
     * SATSolver with general knowledge about the game
     * 
     */

    public ClueReasoner() {
        numPlayers = players.length;

        // Initialize card info
        cards = new String[suspects.length + weapons.length + rooms.length];
        int i = 0;
        // Suspects cards
        for (String card : suspects)
            cards[i++] = card;
        // Weapons cards
        for (String card : weapons)
            cards[i++] = card;
        // Rooms cards
        for (String card : rooms)
            cards[i++] = card;

        numCards = i;

        // Initialize solver
        solver = new SATSolver();
        // Populating the solver with general caluses that expresses general knowledge
        // about the game
        addInitialClauses();
    }

    /**
     * 
     * We look at every element of the array of cards to find an element with a
     * matching String and we return the index of such value.
     * We treat the case file as a special type of player with an index one greater
     * than the last player.
     * Each such index represents a different possible place a card can be. If the
     * caller passes an invalid player String as a parameter, the return value is
     * -1.
     * 
     * @param player: the name of the Player
     * @return the index number of the player represented by the given input
     */
    private int getPlayerNum(String player) {
        if (player.equals(caseFile))
            return numPlayers;
        for (int i = 0; i < numPlayers; i++)
            if (player.equals(players[i]))
                return i;
        System.out.println("Illegal player: " + player);
        return -1;
    }

    /**
     * We look at every element of the array of cards to find an element with a
     * matching String and we return the index of such value.
     * Each such index represents a different possible place a card can be. If the
     * caller passes an invalid card String as a parameter, the return value is
     * -1.
     * 
     * @param card: the name of the card
     * @return the index number of the card represented by the given input
     */
    private int getCardNum(String card) {
        for (int i = 0; i < numCards; i++)
            if (card.equals(cards[i]))
                return i;
        System.out.println("Illegal card: " + card);
        return -1;
    }

    /**
     * Each Clue atomic sentence c_p symbolize the statement " The card c is in
     * place p". There is an atomic sentence for each place and card pair. We assign
     * an integer to each sentence for our DIMACS CNF format as follows:
     * Suppose we have a place index i_p and a card index i_c. Then the integer
     * corresponding to c_p:
     * i_p * numCards + i_c + 1
     * 
     * @param player: the name of the player
     * @param card:   the name of the card
     * @return the assigned integer for the DIMACS CNF of the atomic statement
     */
    private int getPairNum(String player, String card) {
        return getPairNum(getPlayerNum(player), getCardNum(card));
    }

    /**
     * Each Clue atomic sentence c_p symbolize the statement " The card c is in
     * place p". There is an atomic sentence for each place and card pair. We assign
     * an integer to each sentence for our DIMACS CNF format as follows:
     * Suppose we have a place index i_p and a card index i_c. Then the integer
     * corresponding to c_p:
     * i_p * numCards + i_c + 1
     * 
     * @param player: the index of the player
     * @param card:   the index of the card
     * @returnthe assigned integer for the DIMACS CNF of the atomic statement
     */
    private int getPairNum(int playerNum, int cardNum) {
        return playerNum * numCards + cardNum + 1;
    }

    /**
     * Adding the general knowledge about the game whichis known by all players
     * before the start of the game:
     * 1) Each card is in at least one place
     * 2) If a card is in on eplace, it cannot be in another place
     * 3) At leaset on ecard of each categoriy is in the case file
     * 4) No two cards in each category is in the case file
     */
    public void addInitialClauses() {

        // Each card is in at least one place (including case file).
        // Assuming every card exists everywhere
        for (int c = 0; c < numCards; c++) {
            int[] clause = new int[numPlayers + 1];
            for (int p = 0; p <= numPlayers; p++)
                clause[p] = getPairNum(p, c);
            solver.addClause(clause);
        }

        // If a card is one place, it cannot be in another place.
        for (int p = 0; p <= numPlayers; p++) {
            int[] clause = new int[2];
            for (int c = 0; c < numCards; c++) {
                for (int q = 0; q <= numPlayers; q++) {
                    // c1 = card is one place
                    // c2 = the card is in other place
                    // If a card is one place, it cannot be in another place = (c1 => ~c2) equal ~c1
                    // v ~c2

                    if (q != p) {
                        clause[0] = -getPairNum(p, c);
                        clause[1] = -getPairNum(q, c);
                        solver.addClause(clause);
                    }
                }
            }
        }
        // At least one card of each category is in the case file. That is we add clause
        // for each categories: weaons, suspect, rooms
        // Weapon
        int[] clause = new int[weapons.length];
        for (int c = 0; c < weapons.length; c++) {
            clause[c] = getPairNum("cf", weapons[c]); // cf is case file player
        }
        solver.addClause(clause);

        // Suspect
        clause = new int[suspects.length];
        for (int c = 0; c < suspects.length; c++) {
            clause[c] = getPairNum("cf", suspects[c]); // cf is case file player
        }
        solver.addClause(clause);

        // Room
        clause = new int[rooms.length];
        for (int c = 0; c < rooms.length; c++) {
            clause[c] = getPairNum("cf", rooms[c]); // cf is case file player
        }
        solver.addClause(clause);

        // No two cards in each category can both be in the case file.
        // c1 = the card is in case file
        // c2 = another card is in the case file
        // That is, for each categories, c1 => ~c2 equal ~c1 v ~c2

        // Weapons
        for (String cardInCaseFile : weapons) {
            clause = new int[2];
            for (String cardNotInCaseFile : weapons) {
                // 2 cards must be different
                if (!cardInCaseFile.equals(cardNotInCaseFile)) {
                    clause[0] = -getPairNum("cf", cardInCaseFile);
                    clause[1] = -getPairNum("cf", cardNotInCaseFile);
                    solver.addClause(clause);
                }
            }
        }
        // Suspects
        for (String cardInCaseFile : suspects) {
            clause = new int[2];
            for (String cardNotInCaseFile : suspects) {
                // 2 cards must be different
                if (!cardInCaseFile.equals(cardNotInCaseFile)) {
                    clause[0] = -getPairNum("cf", cardInCaseFile);
                    clause[1] = -getPairNum("cf", cardNotInCaseFile);
                    solver.addClause(clause);
                }
            }
        }
        // Rooms
        for (String cardInCaseFile : rooms) {
            clause = new int[2];
            for (String cardNotInCaseFile : rooms) {
                // 2 cards must be different
                if (!cardInCaseFile.equals(cardNotInCaseFile)) {
                    clause[0] = -getPairNum("cf", cardInCaseFile);
                    clause[1] = -getPairNum("cf", cardNotInCaseFile);
                    solver.addClause(clause);
                }
            }
        }

    }

    /**
     * At the beginning of the game, the player is dealt a handof cards.This is
     * private information the player has about what is NOT in the case file. Before
     * beginning of the game, we use the hand method to:
     * 1) Set the player whose perspective we are reasoning from
     * 2) Note that the given cards are in the possession of that player
     * 
     * @param player: the name of player who possess the hand
     * @param cards:  an array of the cards in the given player's hand
     */
    public void hand(String player, String[] cards) {
        playerNum = getPlayerNum(player);
        String[] playerCards = cards;
        int[] clause = new int[1];
        for (String card : playerCards) {
            // Adding to the KB that the card is not in the Case File
            clause[0] = -getPairNum("cf", card);
            solver.addClause(clause);
            // Adding to the KB that the card is within possession of the player
            clause[0] = getPairNum(player, card);
            solver.addClause(clause);
        }

    }

    /**
     * A player may suggest a suspect, a weapon, and a room. When a suggestion of
     * three cards is made, the player to the left of the suggester (clockwise)
     * checks their private cards to see if any of the cards are part of the
     * suggestion. If so , the player must refute the suggestion by privately
     * showing one of these refutin cards to the suggester. If not, the player
     * states that they cannot refute the suggestion, and attention then turns to
     * the next player clockwise. The same procedure applies until it reaches back
     * to the suggestor.
     * 
     * @param suggester: The name of the player who made the suggestion
     * @param card1:     The name of suggested murderer
     * @param card2:     The name of suggested murder weapon
     * @param card3:     The name of the suggested room where the murder took place
     * @param refuter:   The name of the player who refute the suggestion
     * @param cardShown: The name of the card of which the refuter used as evidence
     *                   to disproof the suggestion
     */
    public void suggest(String suggester, String card1, String card2,
            String card3, String refuter, String cardShown) {
        int suggesterNum = getPlayerNum(suggester);
        int refuterNum = 0;
        if (refuter != null) {
            refuterNum = getPlayerNum(refuter);
        }

        // Case 1: There is no refuter.
        // Then no OTHER Player besides the suggester and the case file can have card
        // 1,2,3

        // For all player other than the suggester p and the casefile, for all card
        // 1,2,3,
        // [~(p,c1), ~(p,c2), ~(p,c3)]
        if (refuter == null) {
            for (String player : players) {
                int[] clause = new int[1];
                if (!player.equals(suggester)) {
                    clause[0] = -getPairNum(player, card1);
                    solver.addClause(clause);
                    clause[0] = -getPairNum(player, card2);
                    solver.addClause(clause);
                    clause[0] = -getPairNum(player, card3);
                    solver.addClause(clause);
                }
            }
        }

        // Case2: There is a refuter.
        // For all players between suggester and refuter p, for cards c1,2,3, [~(p,c)]
        // The refuter r has the cardShown, [(r,cardSAhown)]
        // The casefile cf doesnt have the cardShown, [~('cf',cardSAhown)]

        if (refuter != null) {

            // The between players dont have card 1,2,3
            // For all players between suggester and refuter p, for cards c1,2,3, [~(p,c)]
            int i = ++suggesterNum % numPlayers;
            int[] clause = new int[1];
            while (i != refuterNum) {
                clause[0] = -getPairNum(players[i], card1);
                solver.addClause(clause);
                clause[0] = -getPairNum(players[i], card2);
                solver.addClause(clause);
                clause[0] = -getPairNum(players[i], card3);
                solver.addClause(clause);
                i = ++i % numPlayers;
            }
            ;

            // If the card is shown.
            // Card shown != null. The refuter has the card. The case file player doesn't
            // have the card
            // The refuter r has the cardShown, [(r,cardSAhown)]
            // The casefile cf doesnt have the cardShown, [~('cf',cardSAhown)]
            if (cardShown != null) {
                // Then the refuter has the card
                clause = new int[1];
                clause[0] = getPairNum(refuter, cardShown);
                solver.addClause(clause);
                // The case file doesn't have the card
                clause[0] = -getPairNum("cf", cardShown);
                solver.addClause(clause);
            }

            // If the card is not shown
            // Card shown == null. The refuter has one of the card1,2,3
            // The refuter r, for all card1,2,3, [(r,c1),(r,c2),(r,c3)]
            if (cardShown == null) {
                clause = new int[3];
                clause[0] = getPairNum(refuter, card1);
                clause[1] = getPairNum(refuter, card2);
                clause[2] = getPairNum(refuter, card3);
                solver.addClause(clause);
            }
        }

    }

    /**
     * The winner of clue is the first player to correctly make an accusation naming
     * the three cards in the case file.
     * Each player can make an accusation on any one turn, and checks the
     * correctness of the declared accusation by privately looking in the case file.
     * An accusation can only be made once, as the player either win or loses
     * depending on the correctness of the accusation.
     * 
     * @param accuser:   The name of the player who made the accusation
     * @param card1:     The name of the accused murderer
     * @param card2:     The name of the accused murder weapon
     * @param card3:     The name of the accused room where the murder took place
     * @param isCorrect: Whether the accusation is correct
     */
    public void accuse(String accuser, String card1, String card2,
            String card3, boolean isCorrect) {
        // If the accusation is correct
        // isCorrect. Then the case file player has card1,2,3,
        // [('cf',c1)], [('cf',c2)], [('cf',c3)]
        if (isCorrect) {
            int[] clause = new int[1];
            clause[0] = getPairNum("cf", card1);
            solver.addClause(clause);
            clause[0] = getPairNum("cf", card2);
            solver.addClause(clause);
            clause[0] = getPairNum("cf", card3);
            solver.addClause(clause);
        }
        // The accusation is not correct
        // isNotCorrect. Then the case file player doesnt have card1,2,3,
        // [~('cf',c1), ~('cf',c2), ~('cf',c3)]
        else {
            int[] clause = new int[3];
            clause[0] = -getPairNum("cf", card1);
            clause[0] = -getPairNum("cf", card2);
            clause[0] = -getPairNum("cf", card3);
            solver.addClause(clause);
        }
    }

    /**
     * Take in a player and carrd and tests that literal, returning SATSOLVER's
     * integer constatns TRUE, FALSE, and UKNOWN depending on the results of
     * satisfiability testing
     * 
     * @param player: The name of the player
     * @param card:   The set of hand of the player
     * @return SATSolver integer
     */
    public int query(String player, String card) {
        return solver.testLiteral(getPairNum(player, card));
    }

    /**
     * Converting the return codes of SATSOLVER into simple String
     * 
     * @param returnCode:
     * @return
     */
    public String queryString(int returnCode) {
        if (returnCode == SATSolver.TRUE)
            return "Y";
        else if (returnCode == SATSolver.FALSE)
            return "n";
        else
            return "-";
    }

    /**
     * Print out the "detective notepad" indicating the current state of the
     * propositional knowledge about the locations of cards
     */
    public void printNotepad() {
        PrintStream out = System.out;
        for (String player : players)
            out.print("\t" + player);
        out.println("\t" + caseFile);
        for (String card : cards) {
            out.print(card + "\t");
            for (String player : players)
                out.print(queryString(query(player, card)) + "\t");
            out.println(queryString(query(caseFile, card)));
        }
    }

    /**
     * Run a test game on the given example of the game
     * 
     * @param args
     * 
     * 
     */
    public static void main(String[] args) {
        ClueReasoner cr = new ClueReasoner();
        String[] myCards = { "wh", "li", "st" };
        cr.hand("sc", myCards);
        cr.suggest("sc", "sc", "ro", "lo", "mu", "sc");
        cr.suggest("mu", "pe", "pi", "di", "pe", null);
        cr.suggest("wh", "mu", "re", "ba", "pe", null);
        cr.suggest("gr", "wh", "kn", "ba", "pl", null);
        cr.suggest("pe", "gr", "ca", "di", "wh", null);
        cr.suggest("pl", "wh", "wr", "st", "sc", "wh");
        cr.suggest("sc", "pl", "ro", "co", "mu", "pl");
        cr.suggest("mu", "pe", "ro", "ba", "wh", null);
        cr.suggest("wh", "mu", "ca", "st", "gr", null);
        cr.suggest("gr", "pe", "kn", "di", "pe", null);
        cr.suggest("pe", "mu", "pi", "di", "pl", null);
        cr.suggest("pl", "gr", "kn", "co", "wh", null);
        cr.suggest("sc", "pe", "kn", "lo", "mu", "lo");
        cr.suggest("mu", "pe", "kn", "di", "wh", null);
        cr.suggest("wh", "pe", "wr", "ha", "gr", null);
        cr.suggest("gr", "wh", "pi", "co", "pl", null);
        cr.suggest("pe", "sc", "pi", "ha", "mu", null);
        cr.suggest("pl", "pe", "pi", "ba", null, null);
        cr.suggest("sc", "wh", "pi", "ha", "pe", "ha");
        cr.suggest("wh", "pe", "pi", "ha", "pe", null);
        cr.suggest("pe", "pe", "pi", "ha", null, null);
        cr.suggest("sc", "gr", "pi", "st", "wh", "gr");
        cr.suggest("mu", "pe", "pi", "ba", "pl", null);
        cr.suggest("wh", "pe", "pi", "st", "sc", "st");
        cr.suggest("gr", "wh", "pi", "st", "sc", "wh");
        cr.suggest("pe", "wh", "pi", "st", "sc", "wh");
        cr.suggest("pl", "pe", "pi", "ki", "gr", null);
        cr.printNotepad();
        cr.accuse("sc", "pe", "pi", "bi", true);
    }
}
