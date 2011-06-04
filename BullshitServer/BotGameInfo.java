package cw_generic;
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

public class BotGameInfo {

    //Member variables

    //ID mapping to the global Id
    private int m_botId;

    //The hand of cards this bot has
    private Hand m_hand;

    //The game status of this bot
    private GameManager.BotGameStatus m_botStatus;

    //Whether or not the bot called BS last time it was asked
    private boolean m_calledBullshit;

    //Constructor
    public BotGameInfo ( int BotId )
    {
        m_botId = BotId;
        m_hand = new Hand();
        m_botStatus = GameManager.BotGameStatus.INITIALIZED;
        m_calledBullshit = false;
    }

    //Get the gloabl ID for this bot
    public int GetBotId()
    {
        return m_botId;
    }

    //Set the response the bot returned through the switch
    public void SetBSResponse( boolean Response)
    {
        m_calledBullshit = Response;
    }

    //Poll to see if this bot has played all it's cards
    public boolean HasNoCardsLeft()
    {
        if( m_hand.GetNumCards() == 0 )
            return true;
        return false;
    }

    //What was the last resposne from the bot
    public boolean GetBSReply()
    {
        return m_calledBullshit;
    }

    //Get the Game status of the Bot
    public GameManager.BotGameStatus GetStatus()
    {
        return m_botStatus;
    }

    //Set the Game status of the bot
    public void SetStatus( GameManager.BotGameStatus Status )
    {
        m_botStatus = Status;
    }

    //Get the entire hand this bot currently has as a
    //comma seperated string
    public String GetEntireHand()
    {
        return m_hand.GetHandAsString();
    }

    //Poll to see if all the cards in the comma seperated string
    //are actually in this bot's hand
    public boolean HasCards( String CardList )
    {
        //Split up the list
        String[] cardArray = CardList.split(",");

        //array for storing cards / number of that card
        int [] result = new int [Hand.NumCardsInDeck()];
        for( int i = 0; i < Hand.NumCardsInDeck(); i++ )
            result[i] = 0;

        //add the cards to the temp array
        for( int i = 0; i < cardArray.length; i++ ){
            int card = Integer.parseInt( cardArray[i] );
            result[card] = result[card]+1;
        }

        //scroll through the temp array making sure they have the cards
        for( int j = 0; j < Hand.NumCardsInDeck(); j++)
        {
            if( result[j] != 0 )
                if( !m_hand.ValidateHand( j, result[j]) ) return false;
        }

        return true;
    }

    //Remove the cards specified in the comma seperated string from this
    //bots hand
    public void RemoveCards( String CardList ) throws Exception
    {
        //Split up the list
        String[] cardArray = CardList.split(",");

        //array for storing cards / number of that card
        int [] result = new int [Hand.NumCardsInDeck()];
        for( int i = 0; i < Hand.NumCardsInDeck(); i++ )
            result[i] = 0;

        //For each card in the list bump the index
        for( int i = 0; i < cardArray.length; i++ ){
            int card = Integer.parseInt( cardArray[i] );
            result[card] = result[card]+1;
        }

        //Remove the cards found in the list
        for( int j = 0; j < Hand.NumCardsInDeck(); j++)
        {
            if( result[j] != 0 )
                m_hand.RemoveCards( j, result[j] );
        }
    }

       //Takes a comma seperated list of cards and adds them to
    //the players hand.
    public void AddCards( String CardList ) throws Exception
    {
        //Split up the list
        String[] cardArray = CardList.split(",");

        //array for storing cards / number of that card
        int [] result = new int [Hand.NumCardsInDeck()];
        for( int i = 0; i < Hand.NumCardsInDeck(); i++ )
            result[i] = 0;

        //For each card in the list bump the index
        for( int i = 0; i < cardArray.length; i++ ){
            int card = Integer.parseInt( cardArray[i] );
            result[card] = result[card]+1;
        }

        //Remove the cards found in the list
        for( int j = 0; j < Hand.NumCardsInDeck(); j++)
        {
            if( result[j] != 0 )
                 m_hand.InsertCards( j, result[j]);
        }
        
    }

    //DEBUG FUNCTION
    //********************
    public String GetValidMove(int Card)
    {
        while( m_hand.ValidateHand( Card, 1 ) == false )
        {
            Card = (Card+1)%(Hand.NumCardsInDeck());
        }
        return String.valueOf(Card);         
    }

    //Add the specified card to this bots hand
    public void AddCard( int Card ) throws Exception
    {
        m_hand.InsertCards( Card, 1 );
    }
 
}
