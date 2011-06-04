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


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.CallableStatement;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;





/**
 *
 * @author Toby
 */
public class SqlConnector {

    Connection m_connection = null;

    public int LoginBot( int BotId )
    {
        
        int rank = -1;
        ConnectToDb();
       
        Statement stmt = null;
        ResultSet rs = null;


        try {
            stmt = m_connection.createStatement();
            String query = "call LoginBot( " + Integer.toString( BotId ) + ")";
            rs = stmt.executeQuery( query );
            rs.next();
            rank = rs.getInt(1);           
        }
        catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }


        
        return rank;
        
    }


    public void SaveGame( int Winner, int Bot1, int Bot2, int Bot3, int Bot4, String GameHistory )
    {
        ConnectToDb();

        Statement stmt = null;


        try {
            stmt = m_connection.createStatement();
            String query = "call SaveGame( "    + Integer.toString( Winner ) + ","
                                                + Integer.toString( Bot1 )  + ","
                                                + Integer.toString( Bot2 )  + ","
                                                + Integer.toString( Bot3 )  + ","
                                                + Integer.toString( Bot4 )  + ","
                                                + "'" + GameHistory + "'"
                                                + ")";
            stmt.execute(query);
        }
        catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }

    }

    private void ConnectToDb()
    {
        if( m_connection != null ) return;
        String userName = "";
        String password = "";

        try
        {
            
            File file = new File("C:\\APPM\\EEF\\Codewars\\DBInfo\\bs_config.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("DBInfo");

            for (int s = 0; s < nodeLst.getLength(); s++) {
                Node fstNode = nodeLst.item(s);
                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element fstElmnt = (Element) fstNode;
                    NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("UserName");
                    Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                    NodeList fstNm = fstNmElmnt.getChildNodes();
                    userName = ((Node) fstNm.item(0)).getNodeValue();
                    NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("Password");
                    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
                    NodeList lstNm = lstNmElmnt.getChildNodes();
                    password = ((Node) lstNm.item(0)).getNodeValue();
                }

            }
      }
      catch (Exception e) {
        e.printStackTrace();
      }



        try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println( ex.toString() );
        }
        try {
            m_connection = DriverManager.getConnection("jdbc:mysql://localhost/codewars_bs?" + "user=" + userName + "&password=" + password );


        // Do something with the Connection

        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    private void Close()
    {
        try {
            if( m_connection != null)
            {
                m_connection.close();
                m_connection = null;
            }

        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            Close();        // close open files
        }
        finally
        {
            super.finalize();
        }
    }


}
