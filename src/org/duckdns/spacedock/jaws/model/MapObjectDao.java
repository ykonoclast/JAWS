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
package org.duckdns.spacedock.jaws.model;

import java.io.FileNotFoundException;
import java.util.Locale;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.duckdns.spacedock.commonutils.files.GeneralFileHandler;

/**
 * classe d'accès aux fichiers de référence pour les caracs des objets sur carte
 *
 * @author ykonoclast
 */
public class MapObjectDao
{

    /**
     * accesseur pour les fichiers JSON
     */
    private final GeneralFileHandler m_handler;

    /**
     * tableau des caracs de vaisseaux
     */
    private final JsonArray m_shipStats;

    /**
     * compteur, pour création d'identifiant unique pour les objets créés sur la
     * carte.
     */
    private static int m_idCounter = 0;

    /**
     * instance unique det accesseur
     */
    private static MapObjectDao m_instance;

    /**
     *
     * @return un identifiant unique d'objet sur carte
     */
    public int makeId()
    {
	int result = m_idCounter;
	++m_idCounter;
	return result;
    }

    /**
     * constructeur pirvé pour éviter trop d'instanciations
     */
    private MapObjectDao() throws FileNotFoundException
    {
	m_handler = GeneralFileHandler.getInstance("org.duckdns.spacedock.jaws");
	JsonObject object = m_handler.loadJsonFile("mapobjects");

	m_shipStats = object.getJsonArray("Shipstats");
    }

    /**
     * pseudo-constructeur
     *
     * @return une instance unique
     * @throws FileNotFoundException
     */
    public static MapObjectDao getInstance() throws FileNotFoundException
    {
	MapObjectDao result;
	if (m_instance == null)
	{
	    result = new MapObjectDao();
	}
	else
	{
	    result = m_instance;
	}
	return result;
    }

    /**
     *
     * @param p_shipType la chaîne du type telle qu'employée dans les fichiers
     * de configuration
     * @return la Power Curve d'un vaisseau depuis un fichier de configuration
     */
    public Ship.PowerCurve getPowerCurve(String p_shipType)
    {
	Ship.PowerCurve result = null;

	for (JsonValue shipObject : m_shipStats)
	{//pour chaque vaisseau décrit dans le fichier
	    if (((JsonObject) shipObject).getString("type").equals(p_shipType))
	    {//si le type correspond au paramétre on affecte les valeurs de Power Curve
		JsonArray array = ((JsonObject) shipObject).getJsonArray("curve");
		result = new Ship.PowerCurve(array.getInt(0), array.getInt(1), array.getInt(2));
	    }
	}
	if (result == null)
	{
	    m_handler.paramAberrant("typenontrouve", p_shipType, Locale.getDefault());
	}
	return result;
    }
}
