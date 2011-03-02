# -*- coding:utf-8 -*-
"""
Python Game Server
Created on January 21st, 2011


@author: Aman Ahuja
"""
import socket
import threading 

class new(threading.Thread):
    def __init__(self, server_name, server_key,
                 serverIP = 'www.code-wars.com',
                 port = 3000,
                 room_id='null',language='python'):
        
        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.s.connect((serverIP, port))
        
        print "Attempting connection to", serverIP, "at port", port, \
            "with server", server_name    
        self.Login_Str = r'REGISTER<<' + server_name + ':' + \
            server_key # + ':' + language

        print self.Login_Str
        self.sendln(self.Login_Str)
        self.login_resp = self.s.recv(1024)
        print self.login_resp
          
    def sendln(self,string):          
        string_u = unicode(string,"utf-8")
        self.s.send(string_u)
      
    def kill(self):       
        self.destroy()
          
    def destroy(self):      
        self.s.close()
        
    def ping(self):     
        #self.sendln(r"SERVER_PING<<null")
        send_str = r"SERVER_PING<<null"
        self.sendln(send_str)

    def getMsg(self):
        self.command = self.s.recv(1024)
        return self.command
 
    def acceptChallenge(self):         
        reply = 'go'
        send_str = r"GAME_INITIALIZE<<"+reply
        self.sendln(send_str)
    
    #Specific to BULLSHIT
    def sendCards(self,reply):
        send_str = r"PLAYCARDS_REPLY<<"+reply
        print send_str 
        self.sendln(send_str)

    def turnReply(self,reply):
        send_str = r"TURN_REPLY<<"+reply 
        self.sendln(send_str)
    
