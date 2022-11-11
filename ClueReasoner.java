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

public class ClueReasoner 
{

    // Define field variables
    private int numPlayers;
    private int playerNum;
    private int numCards;
    private SATSolver solver;    
    private String caseFile = "cf";
    private String[] players = {"sc", "mu", "wh", "gr", "pe", "pl"};
    private String[] suspects = {"mu", "pl", "gr", "pe", "sc", "wh"};
    private String[] weapons = {"kn", "ca", "re", "ro", "pi", "wr"};
    private String[] rooms = {"ha", "lo", "di", "ki", "ba", "co", "bi", "li", "st"};
    private String[] cards;


    // Construct ClueReasoner
    public ClueReasoner()
    {
        numPlayers = players.length;

        // Initialize card info
        cards = new String[suspects.length + weapons.length + rooms.length];
        int i = 0;
        for (String card : suspects)
            cards[i++] = card;
        for (String card : weapons)
            cards[i++] = card;
        for (String card : rooms)
            cards[i++] = card;
        numCards = i;

        // Initialize solver
        solver = new SATSolver();
        addInitialClauses();
    }


    // Get the plaer number
    private int getPlayerNum(String player) 
    {
        if (player.equals(caseFile))
            return numPlayers;
        for (int i = 0; i < numPlayers; i++)
            if (player.equals(players[i]))
                return i;
        System.out.println("Illegal player: " + player);
        return -1;
    }

    //Get the card number
    private int getCardNum(String card)
    {
        for (int i = 0; i < numCards; i++)
            if (card.equals(cards[i]))
                return i;
        System.out.println("Illegal card: " + card);
        return -1;
    }


    // Get the literal integer for the player-card pair
    private int getPairNum(String player, String card) 
    {
        return getPairNum(getPlayerNum(player), getCardNum(card));
    }

    private int getPairNum(int playerNum, int cardNum)
    {
        return playerNum * numCards + cardNum + 1;
    }    

    public void addInitialClauses() 
    {
        // TO BE IMPLEMENTED AS AN EXERCISE
        
        // Each card is in at least one place (including case file).
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
                    // c1 =  card is one place
                    // c2 = the card is in other place
                    // If a card is one place, it cannot be in another place = (c1 => ~c2) equal ~c1 v ~c2
                    
                    if (q != p) {
                        clause[0] = -getPairNum(p, c);
                        clause[1] = -getPairNum(q, c);
                        solver.addClause(clause);
                    }
                }
            }
        }
        // At least one card of each category is in the case file. That is we add clause for each categories: weaons, suspect, rooms
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
       
    
    // Add player hand information
    public void hand(String player, String[] cards) 
    {
        playerNum = getPlayerNum(player);

        // TO BE IMPLEMENTED AS AN EXERCISE
        String[] playerCards = cards;
        int[] clause = new int[1];
        for(String card: playerCards)
        {
            clause[0] = -getPairNum("cf",card);
            solver.addClause(clause);
            clause[0] = getPairNum(player,card);
            solver.addClause(clause);
        }

    }


    // Add suggestion information
    public void suggest(String suggester, String card1, String card2, 
                        String card3, String refuter, String cardShown) 
    {
        // TO BE IMPLEMENTED AS AN EXERCISE
    }

    public void accuse(String accuser, String card1, String card2, 
                       String card3, boolean isCorrect)
    {
        // TO BE IMPLEMENTED AS AN EXERCISE
    }

    public int query(String player, String card) 
    {
        return solver.testLiteral(getPairNum(player, card));
    }

    public String queryString(int returnCode) 
    {
        if (returnCode == SATSolver.TRUE)
            return "Y";
        else if (returnCode == SATSolver.FALSE)
            return "n";
        else
            return "-";
    }
        
    public void printNotepad() 
    {
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
        
    public static void main(String[] args) 
    {
        ClueReasoner cr = new ClueReasoner();
        String[] myCards = {"wh", "li", "st"};
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
