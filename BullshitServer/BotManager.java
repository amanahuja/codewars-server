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

public class BotManager  {            
    private int bid;             // Bot Id (sent from switch)
    private boolean busy;        // True when bot is involved in a game. False otherwise.
    private int gid;             // game id (when currently involved in a game)
    private int mode;            // Integer flag representing mode: debug=0, Live/Competition=1.  Currently broken in c4.
    private int rank;
    
    //Constructor
    public BotManager(int bid, int rank){
        this.bid = bid;
        this.rank = rank;
    }
    public int getBID(){
        return this.bid;
    }
    public int getRank(){
        return this.rank;
    }
    public int getGID(){
        return this.gid;
    }
    public void setGID(int in){
        this.gid = in;
    }
    public int getMode(){
        return this.mode;
    }
    public void setMode(int mode){
        this.mode=mode; // 1 live 0 Debug
    }
    public void setBusy(boolean in){
        this.busy=in;
    }
    public boolean isBusy(){
        return this.busy;
    }

 
 }
