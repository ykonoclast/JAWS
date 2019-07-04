/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duckdns.spacedock.jaws.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.duckdns.spacedock.jaws.control.GameManager;
import org.duckdns.spacedock.jaws.model.MapObject;
import org.duckdns.spacedock.jaws.model.Ship;

/**
 * Une servlet ma foi fort bien nommée
 * 
 * @author ykonoclast
 */
public class HorribleServlet extends HttpServlet
{
    //allez, mettons des chaînes en dur, là on est dans l'état de l'art
    private final String m_title = "Java Astral Warfare Simulator";
    
    private GameManager m_manager;
    

    
    private  GameManager.ImpulseReport m_report;
    
    @Override
    public void init() throws ServletException
    {
        try
        {
            m_manager = new GameManager("scenar2");//un seul scénar pour l'instant
        }
        catch(FileNotFoundException e)
        {
            //mange silencieusement l'exception comme un gros dégueulasse
        }
        m_report = m_manager.startGame();
    }
    
    
    

    //Allez, une seule méthode, on utilise le GET pour tout et on rebalance l'intégralité de la page à chaque fois, magnifique.
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
    {


        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>"+m_title+"</h1>");
        
        
        
        
     
        
        


        if (req.getParameterMap().containsKey("id")) {

            

            String str1 = req.getParameter("av");       //  av submit button
            String str2 = req.getParameter("turn");       //  turn submit button
            String str3 = req.getParameter("end");       //  end submit button

            //remplacer par ServletOutputStream pour println


            if (str1 != null) // av
            {
                int id = Integer.parseInt(req.getParameter("id"));
                m_report = m_manager.moveShipStraight(id);
            } 
            else if (str2 != null) // turn
            {
                int id = Integer.parseInt(req.getParameter("id"));
                MapObject.Orientation orient = null;
                switch(req.getParameter("orient"))
                {
                    case "NE": orient = MapObject.Orientation.NE; break;
                    case "E": orient = MapObject.Orientation.E;break;
                    case "SE": orient = MapObject.Orientation.SE;break;
                    case "SW": orient = MapObject.Orientation.SW;break;
                    case "W": orient = MapObject.Orientation.W;break;
                    case "NW": orient = MapObject.Orientation.NW;break;
                }
                m_report = m_manager.turnShip(id, orient);
            }
            else if (str3 != null) // turn
            {
                m_report = m_manager.advanceImpulse();
            }


        }
        
        
        
        
        
        
        
        //impulsion courante
        String impulsion = "<h2>Impulsion "+m_report.currentImpulse+"</h2>";
        String joueur = "<h2>Joueur : "+m_report.currentPlayer+"</h2>";
        
        //liste de tous les vaisseaux en jeu ainsi que de ceux devant bouger
        Map<GameManager.Player, List<Ship>> listAllShips = m_manager.getAllShips();
        String listTotale ="<h2>Liste des vaisseaux en jeu :</h2>";
        String list2Move ="<h2>Liste des vaisseaux devant bouger :</h2>";
        listTotale = listTotale.concat("<h3>"+GameManager.Player.TALON.toString()+" :</h3>");
        list2Move = list2Move.concat("<h3>"+GameManager.Player.TALON.toString()+" :</h3>");

        
        for(Ship ship : listAllShips.get(GameManager.Player.TALON))
        {
            listTotale = listTotale.concat(ship.getId()+" "+ship.toString()+" "+ship.getCoordinates().toString()+"<br />");
            if(m_report.mustMoveShips.contains(ship.getId()))
            {
                list2Move= list2Move.concat(ship.getId()+" "+ship.toString()+"<br />");
            }
        }
        listTotale = listTotale.concat("<h3>"+GameManager.Player.TERRAN.toString()+" :</h3>");
        list2Move = list2Move.concat("<h3>"+GameManager.Player.TERRAN.toString()+" :</h3>");
        for(Ship ship : listAllShips.get(GameManager.Player.TERRAN))
        {
            listTotale = listTotale.concat(ship.getId()+" "+ship.toString()+"<br />");
            if(m_report.mustMoveShips.contains(ship.getId()))
            {
                list2Move= list2Move.concat(ship.getId()+" "+ship.toString()+"<br />");
            }
        }
        
        String form = "<form method=\"get\" action=\"http://localhost:8080/\"><b>id de vaisseau<input type=\"text\" name=\"id\"><br><b>nouvelle orientation<input type=\"text\" name=\"orient\"><br><input type=\"submit\" value=\"avancer\" name=\"av\"><input type=\"submit\" value=\"tourner\" name=\"turn\"><br><input type=\"submit\" value=\"fin de tour\" name=\"end\"></b></form>";
        
        

        response.getWriter().println("<body>"+impulsion+joueur+listTotale+list2Move+form+"</body>");

    }

}
