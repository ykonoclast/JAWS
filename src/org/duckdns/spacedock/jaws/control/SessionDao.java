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
package org.duckdns.spacedock.jaws.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.duckdns.spacedock.jaws.model.MapObject;
import org.duckdns.spacedock.jaws.model.Ship;
import org.duckdns.spacedock.commonutils.files.GeneralFileHandler;
import org.duckdns.spacedock.jaws.control.GameManager.Impulse;

/**
 * Classe d'accès aux éléments genéraux d'une partie (scénario joué, sauvegarde
 * des éléments de jeu...) pas de référence métier ici
 *
 * @author ykonoclast
 */
class SessionDao
{

    /**
     * tableau des power curve jouables par Impulse
     */
    private final Map<Impulse, List<Integer>> m_curveByImpulse = new EnumMap<>(Impulse.class);

    /**
     * instance privée singleton
     */
    private static SessionDao m_instance;

    /**
     * accesseur pour les fichiers JSON
     */
    private final GeneralFileHandler m_handler;

    /**
     * url de la base SQLite
     */
    private final String m_urlBase;

    /**
     * nom du fichier de base de données
     */
    private final String m_databaseFileName = "jaws-data.db";//TODO à passer en paramétre plutôt qu'en dur comme ici

    /**
     * répertoire dans lequel se trouve la base
     */
    private final File m_databaseRoot;//TODO à passer en paramétre plutôt qu'en dur comme ici

    /**
     * constructeur privé pour éviter trop d'instanciations
     */
    private SessionDao() throws FileNotFoundException, URISyntaxException, ClassNotFoundException, SQLException
    {
	m_handler = GeneralFileHandler.getInstance("org.duckdns.spacedock.jaws");

	for (Impulse key : Impulse.values())
	{
	    m_curveByImpulse.put(key, new ArrayList<>());
	}

	JsonArray array = m_handler.loadJsonFile("sessionconfig").getJsonArray("listimpulses");
	array.forEach((impulseBlock) ->
	{
	    //pour chaque impulsion, on lit de quelle impulsion il s'agit
	    String jsonImpulse = ((JsonObject) impulseBlock).getString("impulse");
	    Impulse impulse = Impulse.A;

	    switch (jsonImpulse)
	    {
		case "A":
		    impulse = Impulse.A;
		    break;
		case "B":
		    impulse = Impulse.B;
		    break;
		case "C":
		    impulse = Impulse.C;
		    break;
		case "D":
		    impulse = Impulse.D;
		    break;
		case "E":
		    impulse = Impulse.E;
		    break;
		case "F":
		    impulse = Impulse.F;
		    break;
		default:
		    m_handler.paramAberrant("BadImpulseJSON", jsonImpulse, Locale.getDefault());
	    }

	    //puis on lit les valeurs de power curve afférentes à l'impulse
	    JsonArray jsonCurve = ((JsonObject) impulseBlock).getJsonArray("curve");

	    //on stocke le tout dans une EnumMap
	    for (JsonValue number : jsonCurve)
	    {
		m_curveByImpulse.get(impulse).add(((JsonNumber) number).intValue());
	    }

	});

	//on ouvre la connexion à la base
	Class.forName("org.sqlite.JDBC");
	m_databaseRoot = new File(SessionDao.class.getProtectionDomain().getCodeSource().getLocation().toURI());
	m_urlBase = "jdbc:sqlite:" + m_databaseRoot.getAbsoluteFile().getParent() + "/" + m_databaseFileName;

	//création des tables si elles n'existent pas
	String sql = "CREATE TABLE IF NOT EXISTS moves (id integer,turn integer,impulse text,mvt text NOT NULL,PRIMARY KEY(id, turn, impulse));";
	executeSQLStatement(sql);
    }

    /**
     * pseudo-constructeur statique
     *
     * @return
     */
    static SessionDao getInstance() throws FileNotFoundException, URISyntaxException, ClassNotFoundException, SQLException
    {
	SessionDao result;
	if (m_instance == null)
	{
	    result = new SessionDao();
	}
	else
	{
	    result = m_instance;
	}
	return result;
    }

    /**
     * charge un scénario en début de partie à partir des fichiers JSON de
     * référence
     *
     * @param p_scenario nom du fichier (avec ou sans extension, préciser le
     * répertoire si ce n'est pas la racine du répertoire scenarii des
     * resources)
     * @param p_listTalonShips liste issue du GameManager, sera remplie ici
     * @param p_listTerranShips liste issue du GameManager, sera remplie ici
     * @throws FileNotFoundException
     */
    void loadScenario(String p_scenario, List<Ship> p_listTalonShips, List<Ship> p_listTerranShips) throws FileNotFoundException
    {
	JsonObject scenar = m_handler.loadJsonFile("scenarii/" + p_scenario);

	JsonArray talonShips = scenar.getJsonArray("Talon ships");
	JsonArray terranShips = scenar.getJsonArray("Terran ships");

	for (JsonValue ship : talonShips)
	{
	    p_listTalonShips.add(makeShip((JsonObject) ship));
	}

	for (JsonValue ship : terranShips)
	{
	    p_listTerranShips.add(makeShip((JsonObject) ship));
	}
    }

    /**
     * méthode factory produisant des objets Ship à partir d'un JsonObject
     * respectant le bon format
     *
     * @param p_object
     * @return
     */
    private Ship makeShip(JsonObject p_object) throws FileNotFoundException
    {
	String name = p_object.getString("name");
	String type = p_object.getString("type");

	int posL = p_object.getInt("posL");
	int posC = p_object.getInt("posC");

	MapObject.Orientation orientation;
	String JSONorientation = p_object.getString("orientation");

	switch (JSONorientation)
	{
	    case "NE":
		orientation = MapObject.Orientation.NE;
		break;
	    case "E":
		orientation = MapObject.Orientation.E;
		break;
	    case "SE":
		orientation = MapObject.Orientation.SE;
		break;
	    case "SW":
		orientation = MapObject.Orientation.SW;
		break;
	    case "W":
		orientation = MapObject.Orientation.W;
		break;
	    case "NW":
		orientation = MapObject.Orientation.NW;
		break;
	    default:
		orientation = MapObject.Orientation.NE;//tolérance aux erreurs : si l'orientation n'est pas une chaîne conforme, le vaisseau pointe au NE
	}
	return new Ship(type, name, new MapObject.HexCoordinates(posL, posC, orientation));
    }

    private void executeSQLStatement(String p_SQLCode) throws SQLException
    {
	//try with resources pour tout fermer automatiquement si ça part mal
	try (Connection conn = DriverManager.getConnection(m_urlBase);
		Statement stmt = conn.createStatement())
	{
	    if (conn != null)
	    {
		stmt.execute(p_SQLCode);
	    }
	}
	catch (SQLException e)
	{
	    throw e;//on renvoie juste l'exception : à voir si plus haut on souhaite afficher ou juste logger
	}
    }

    void storeMove(int p_idShip, int p_turn, Impulse p_impulse, MapObject.HexCoordinates p_coord) throws SQLException
    {
	String sql = "INSERT INTO moves VALUES (" + p_idShip + "," + p_turn + "," + "\"" + p_impulse.toString() + "\"" + "," + "\"" + p_coord.toString() + "\"" + ");";
	executeSQLStatement(sql);
    }

    /**
     *
     * @param p_impulse une impulsion
     * @return les valeurs de Power Curve ouvrant droit à action ou mouvement
     * dans cette Impulse
     * @throws FileNotFoundException
     */
    List<Integer> getCurveByImpulse(GameManager.Impulse p_impulse)
    {
	return m_curveByImpulse.get(p_impulse);
    }
}
