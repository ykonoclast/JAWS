/*
 * Copyright (C) 2019 ykonoclast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.duckdns.spacedock.jaws.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 *
 * @author ykonoclast
 */
public class JawsLauncher
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
	Server server = new Server(8080);//à passer en paramétre avec valeur par défaut
         
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        
        handler.addServletWithMapping(HorribleServlet.class, "/*");//quelle bonne idée, balançons tout sur la même URL, droit dans la racine, ça sert à rien les arborescences
        
        server.start();        
        server.join();//rien fait pour s'arrêter proprement, se fera une joie de polluer l'espace d'adresses
    }
}
