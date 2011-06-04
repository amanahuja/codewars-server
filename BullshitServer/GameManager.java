/*
 // ************************************************************
//
//  Copyright 2010 Department of Applied Mathematics (APPM) at the
//		       University of Colorado at Boulder (UCB)
//
//  Revision History:
//  <12/18/2010	tmj		Version for release to codewars>
//
//  Confidential: Not for use or disclosure outside APPM-UCB without
//                        prior written consent.
//
// ***********************************************************

 */

package cw_generic;

import org.apache.log4j.Logger;

public class GameManager {

    //enums for tracking states of bots
    public enum BotGameStatus { INITIALIZED, ACCEPTED_CHALLENGE, WAITING_ON_BSRESPONSE, READY };
    //enums for tracking the state of the game
    public enum GameStatus{ WAITING_TO_START, GAME_RUNNING, WAITING_ON_BULLSHIT };

    //static variable from previous implementation that I don't like
    private static int currentgid = 1;
    
    //This instances id
    private int gid;

    //The info about the players / cards for this game
    private BotGameInfo[] m_bots;
    private Hand m_pile;

    //for debug and info logging
    private Logger logger;
    
    //store the current state of the game
    private int m_currentMoveCard;
    private int m_currentPosition;
    private GameStatus m_status;
    private int m_winnerId;

    //Store the last move made until all bots have responded
    private int m_lastCount;
    private int[] m_lastCards;

    private String m_gameHistory;

    //Each game manager can be initialized to any number of bots that you want
    //dynamicall on initilization
    public GameManager(int[] botIDs, Logger logger ){
       
        //Create an array for the bots for this game
        m_bots = new BotGameInfo[botIDs.length];

        //create bots for each botId
        for( int i = 0; i < botIDs.length; i++ )
            m_bots[i] = new BotGameInfo( botIDs[i] );

        //Create array for last move cards
        m_lastCards = new int[4];

        //set this games' ID and bump the global static
        this.gid = currentgid;
        currentgid++;

        //create the hand
        m_pile = new Hand();

        //keep a reference to the global logger
        this.logger = logger;

        //Set the current game state
        m_currentMoveCard = 0;
        m_currentPosition = 0;
        m_status = GameStatus.WAITING_TO_START;
        m_winnerId = -1;

        m_gameHistory = "";
    }

    //Request the Gamge Manager to redeal the deck to all players in the game
    //This also resets the game state.
    public void DealCardsForNewGame()
    {
        //set up the deck
        int currentPosition = 0;
        int CardsDealt = 52;
        m_pile.FillDeck();

        //Until you have pulled all the cards randomly pull a card from the deck
        //and assign it to the next player.
        while( CardsDealt > 0 )
        {
            int card = m_pile.PullRandomCard();
            try
            {
                m_bots[currentPosition].AddCard( card );
            }
            catch( Exception ex )
            {
                logger.error( "Failed to deal card with error; " + ex.getMessage() );
            }
            currentPosition++;
            currentPosition = currentPosition%GetNumPlayers();
            CardsDealt--;
        }

        //Reset game state
        m_currentMoveCard = 0;
        m_currentPosition = 0;
        m_status = GameStatus.GAME_RUNNING;
        m_winnerId = -1;

        //Set all the bots as ready
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            m_bots[i].SetStatus(BotGameStatus.READY);
        }
    }

    public String GetGameHistory()
    {
        return m_gameHistory;
    }
    public void AddMoveForHistory( String Move )
    {
        m_gameHistory += Move;
        m_gameHistory += "\n";
        logger.info( Move );
    }
    //Set the game as waiting on a bullshit call
    public void SetWaitingForBullShit()
    {
        m_status = GameStatus.WAITING_ON_BULLSHIT;
    }

    public boolean isWaiting( int BotId )
    {
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( m_bots[i].GetBotId() == BotId)
            {
                if( m_bots[i].GetStatus() == BotGameStatus.INITIALIZED ) return true;
            }
        }
        return false;
    }

    //Set a bot as ready to play after an initialize request
    public void setReady( int BotId )
    {
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( m_bots[i].GetBotId() == BotId)
            {
                m_bots[i].SetStatus( BotGameStatus.ACCEPTED_CHALLENGE );
            }
        }
    }

    //Store this players response to the last bullshit request
    public void SetBSResponse( int BotId, boolean Response )
    {
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( m_bots[i].GetBotId() == BotId )
            {
                m_bots[i].SetBSResponse( Response );
                m_bots[i].SetStatus( BotGameStatus.READY );
            }
        }
    }

    //Return whether or not all bots have responded to the game initialize request
    public boolean AllBotsHandledTurn()
    {
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( m_bots[i].GetStatus() != BotGameStatus.READY )
                return false;
        }

        m_status = GameStatus.GAME_RUNNING;
        return true;
    }

    //Get the Bot if of which bot lost the bullshit challenge
    //If the person who played the cards was lieing then they are returned
    //Otherwise the first person in the order of play after that person is returned
    public int GetBullShitLoser()
    {
        //find the next person who called bullshit
        int botID = -1;
        int index  = (m_currentPosition + 1)%(GetNumPlayers());
        int numChecked = 1;
        if( m_bots[index].GetBSReply() == true )
            botID = m_bots[index].GetBotId();
        while( botID == -1 && numChecked < GetNumPlayers() )
        {
            index = (index + 1)%(GetNumPlayers());
            if( m_bots[index].GetBSReply() == true )
                botID = m_bots[index].GetBotId();
        }

        //figure out whether it was a lie or not
        boolean wasBullshit = false;
        for( int i = 0; i < m_lastCount; i++ )
        {
            if( m_lastCards[i] != this.m_currentMoveCard )
                wasBullshit = true;
        }
        
        //return who will receive the cards
        if( wasBullshit )
        {
            return m_bots[m_currentPosition].GetBotId();
        }
        return botID;
    }

    //This routine builds the summary that is sent to all the players after a bullshit is called
    //It returns to all the players 3 peices of info
    //  1) The person who was credited with calling bullshit
    //  2) Whether they were right
    //  3) What cards were played in the turn they called bullshit on
    public String GetBullShitCallSummary()
    {
        int botID = -1;
        int index  = (m_currentPosition + 1)%(GetNumPlayers());
        int numChecked = 1;
        if( m_bots[index].GetBSReply() == true )
            botID = m_bots[index].GetBotId();
        while( botID == -1 && numChecked < GetNumPlayers() )
        {
            index = (index + 1)%(GetNumPlayers());
            if( m_bots[index].GetBSReply() == true )
                botID = m_bots[index].GetBotId();
        }


        String response = "";
        response += String.valueOf( botID );
        String cards = "";
        boolean wasBullshit = false;
        for( int i = 0; i < m_lastCount; i++ )
        {
            if( i != 0 )
                cards += ",";
            cards += String.valueOf( m_lastCards[i] );
            if( m_lastCards[i] != this.m_currentMoveCard )
                wasBullshit = true;
        }
        
        String bsValue = "";
        if( wasBullshit )
        {
            bsValue += "true";
        }
         else
        {
            bsValue += "false";
        }

        //dones this way so I can have order match design document
        response += ":";
        response += bsValue;
        response += ":";
        response += cards;

      //  logger.info( "Bullshit response summary :" + response );
        return response;
    }

    //Once all the notification has been completed build the list of cards that
    //need to be given to the loser and give them to him, returns that string so it
    //can be send to the bot through the switch
    public String ProcessBullshit()
    {
        //get the loser and the cards
        int botID = GetBullShitLoser();
        String cardList = m_pile.GetHandAsString();
        m_pile.Reset();

        //look up the index
        int index = 0;
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( m_bots[i].GetBotId() == botID )
                index = i;
        }

        AddMoveForHistory( "Bot <" + botID + "> received the cards " + cardList + " due to a bullshit call");
        //hand back the cards
        try
        {
            m_bots[index].AddCards(cardList);
        }
        catch( Exception ex )
        {
            logger.error( "Failed to assign cards from BS call : " + ex.getMessage() );
        }
        return cardList;
    }

    //Run through all the bots and identify if bullshit was called
    public boolean BSCalled()
    {
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( m_bots[i].GetBSReply() == true)
            {
                return true;
            }
        }
        return false;
    }

    //Get the ID of the bot whose turn it is
    public int GetCurrentPlayerId()
    {
        return m_bots[m_currentPosition].GetBotId();
    }

    //Get the current card that needs to be played as a string
    public String GetCurrentMoveCard()
    {
        return String.valueOf(m_currentMoveCard);
    }

    //Have all the bots replied to GAME_INITIALIZE
    public boolean allBotsReady()
    {
        for( int i = 0; i < GetNumPlayers(); i++)
        {
            if( m_bots[i].GetStatus() != BotGameStatus.ACCEPTED_CHALLENGE )
                return false;
        }
        return true;
    }

    //Has anyone won the game at this point.
    //This is solely based on if someone has no cards
    public boolean HasWinner()
    {
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( m_bots[i].HasNoCardsLeft())
            {
                m_winnerId = m_bots[i].GetBotId();
                return true;
            }
        }
        return false;
    }

    //Move onto the next player / card in the rotation
    public void AdvanceTurn()
    {
        m_currentMoveCard = (m_currentMoveCard+1)%(Hand.NumCardsInDeck());
        m_currentPosition = (m_currentPosition+1)%(GetNumPlayers());
    }

    //Get the winners ID
    public int GetWinnerId()
    {
        return m_winnerId;
    }

    //Return the summary of the last term to be broadcast
    //returns three pieces of information
    //  1)The bot who played
    //  2)The card they were supposed to play
    //  3)How many of that card they claimed to play
    //formatted like <botid>:<card>:<number>
    public String GetTurnSummary()
    {
        String turnSummary = "";
        
        turnSummary += m_bots[m_currentPosition].GetBotId();
        turnSummary += ":";
        turnSummary += m_currentMoveCard;
        turnSummary += ":";
        turnSummary += m_lastCount;
        
       // logger.info( "TurnSummary: " + turnSummary );
        return turnSummary;
    }

    //Store the information from a move returned through the switch
    //This returns true if it was a legal move
    public boolean SetLastPlayerMove( int BotId, String CardList )
    {
        //don't let people go out of turn
        if( BotId != m_bots[m_currentPosition].GetBotId() )
        {
            return false;
        }

        //Split the string
        String[] cardArray = CardList.split(",");
        m_lastCount = cardArray.length;

        //look though adding the cards to the deck
        for( int i = 0; i < m_lastCount; i++ )
        {
            m_lastCards[i] = Integer.parseInt( cardArray[i] );
            try
            {
                m_pile.InsertCards( m_lastCards[i] , 1);
            }
            catch( Exception ex)
            {
                logger.error( "Failed to add cards to deck : " + ex.getMessage() );
            }
        }

        //remove the added cards from the bot who played them
        try
        {
            m_bots[m_currentPosition].RemoveCards( CardList );
        }
        catch( Exception Ex)
        {
            logger.error( "Failed to remove cards from a bot I should have been able to! : " + Ex.getMessage() );
        }

        //Set all the players other than this one to waiting on a BS state
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( m_bots[i].GetBotId() != BotId )
                m_bots[i].SetStatus(BotGameStatus.WAITING_ON_BSRESPONSE);
            else
            {
                m_bots[i].SetStatus(BotGameStatus.READY);
                m_bots[i].SetBSResponse( false );
            }
        }

        return true;
    }

    //Routine that checks to see if a response from a bot through the switch
    //actually contains a valid card set
    public boolean ContainsValidCardSet( String CardList )
    {
        if( CardList.equals( "" )) return false;
        
        String[] cardArray = CardList.split(",");
        if( cardArray.length == 0 ) return false;
        for( int i = 0; i < cardArray.length; i++)
        {
            try
            {
                int card = Integer.parseInt( cardArray[i] );
                if( card < 0 ) return false;
                if( card >= Hand.NumCardsInDeck() ) return false;
            }
            catch( Exception e)
            {

                logger.info( "Invalid card cast to int" + CardList );
                return false;
            }
        }
        return true;
        
    }

    //Check to see if a move returned from the swtich is valid for that bot
    public boolean IsValidMove( int BotId, String CardList )
    {
        if( !ContainsValidCardSet( CardList ) ) return false;

        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( m_bots[i].GetBotId() == BotId )
            {
                return m_bots[i].HasCards(CardList);
            }
        }
        
        return false;
    }

    //How many players there are in this game
    public int GetNumPlayers()
    {
        return m_bots.length;
    }

    //Look up the Id for this move
    public int GetIdForCurrentMove()
    {
        return m_bots[m_currentPosition].GetBotId();
    }

    //Get the entire hand for a bot
    public String GetEntireHandByPosition( int Position )
    {
        String hand = m_bots[Position].GetEntireHand();
        AddMoveForHistory( "Dealt hand " + hand + " to bot " + m_bots[Position].GetBotId() );
        return hand;
    }

    //Get a formatted string for the seating of the players
    public String GetSeatingChart()
    {
        String returnMessage = "";
        for( int i = 0; i < GetNumPlayers(); i++ )
        {
            if( i != 0 ) returnMessage += ",";
            returnMessage += m_bots[i].GetBotId();
        }
        return returnMessage;

    }

    //look up the id of a bot by their position in the game
    public int GetBotIdByPosition( int Position )
    {
        return m_bots[Position].GetBotId();
    }

    //Get the id of this instance of the game
    public int getGID(){
        return gid;
    }

    //Look up the last instance of a game that was created
    public static int GetLastGameId()
    {
        return currentgid;
    }
}