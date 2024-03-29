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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.duckdns.spacedock.jaws.control.GameManager.Player;
import org.duckdns.spacedock.jaws.model.MapObject;
import org.duckdns.spacedock.jaws.model.Ship;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ykonoclast
 */
public class SessionManagerIntegTest
{

    private GameManager testee;

    @Before
    public void setUpForEach() throws FileNotFoundException, URISyntaxException, ClassNotFoundException, SQLException
    {
	testee = new GameManager("scenar2");
    }

    @AfterClass
    public static void cleanUpAtTheEnd() throws URISyntaxException
    {
	File root = new File(SessionDao.class.getProtectionDomain().getCodeSource().getLocation().toURI());
	File fullPath = new File(root.getAbsoluteFile().getParent() + "/jaws-data.db");
	fullPath.delete();
    }

    /**
     * teste à la fois la méthode startGame, getAllShips et la méthode
     * advanceInit : toutes sont très liées
     */
    @Test
    public void initTestNominal()
    {
	//commencement de la partie
	GameManager.ImpulseReport report = testee.startGame();

	//récupération des id des vaisseaux
	Map<GameManager.Player, List<Ship>> allShips = testee.getAllShips();
	Map<String, Integer> indShips = new HashMap<>();//map qui va récupérer les indices de tous les vaisseaux du scénar 2
	Assert.assertEquals(5, allShips.get(Player.TALON).size() + allShips.get(Player.TERRAN).size());

	//récupération de tous les indices des vaisseaux
	allShips.get(Player.TERRAN).forEach((ship) ->
	{
	    indShips.put(ship.toString(), ship.getId());
	});
	allShips.get(Player.TALON).forEach((ship) ->
	{
	    indShips.put(ship.toString(), ship.getId());
	});

	//on est au premier tour de l'impulsion A, le joueur actif est le Talon, aucun vaisseau actif, aucun vaisseau à bouger
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.A.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());

	//on passe au second tour de l'impulsion A : joueur actif Terran, aucun vaisseau actif, aucun vaisseau à bouger
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TERRAN.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.A.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());

	//on passe au premier tour de l'impulsion B : joueur actif Talon, actif :  ; mvt : Surprise, Hunter, Shadow
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.B.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon FF Surprise")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon DD Hunter")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon DD Shadow")));
	Assert.assertEquals(3, report.mustMoveShips.size());

	//on passe au second tour de l'impulsion B : joueur actif Terran, actif : Thor ; mvt : Thor, Caleb
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TERRAN.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.B.toString(), report.currentImpulse);
	Assert.assertTrue(report.canActShips.contains(indShips.get("Terran CL Thor")));
	Assert.assertEquals(1, report.canActShips.size());
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Terran CL Thor")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Terran DD Caleb")));
	Assert.assertEquals(2, report.mustMoveShips.size());

	//impulsion C tour 1: joueur actif Talon, actif : Surprise ; mvt : Personne
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.C.toString(), report.currentImpulse);
	Assert.assertTrue(report.canActShips.contains(indShips.get("Talon FF Surprise")));
	Assert.assertEquals(1, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());

	//impulsion C tour 2 : joueur actif Terran, actif :  ; mvt : Caleb
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TERRAN.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.C.toString(), report.currentImpulse);
	Assert.assertTrue(report.canActShips.contains(indShips.get("Terran DD Caleb")));
	Assert.assertEquals(1, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());

	//impulsion D tour 1 : joueur actif Talon, actif : ; mvt : Surprise, Hunter, Shadow
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.D.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon FF Surprise")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon DD Hunter")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon DD Shadow")));
	Assert.assertEquals(3, report.mustMoveShips.size());

	//impulsion D tour 2 : joueur actif Terran, actif : Thor ; mvt : Thor
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TERRAN.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.D.toString(), report.currentImpulse);
	Assert.assertTrue(report.canActShips.contains(indShips.get("Terran CL Thor")));
	Assert.assertEquals(1, report.canActShips.size());
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Terran CL Thor")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Terran DD Caleb")));
	Assert.assertEquals(2, report.mustMoveShips.size());

	//impulsion E tour 1 : joueur actif Talon, actif :  ; mvt :
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.E.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());

	//impulsion E tour 2 : joueur actif Terran, actif :  ; mvt :
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TERRAN.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.E.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());

	//impulsion F tour 1 : joueur actif Talon, actif : Surprise, Hunter, Shadow ; mvt : Surprise, Hunter, Shadow
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.F.toString(), report.currentImpulse);
	Assert.assertTrue(report.canActShips.contains(indShips.get("Talon FF Surprise")));
	Assert.assertTrue(report.canActShips.contains(indShips.get("Talon DD Hunter")));
	Assert.assertTrue(report.canActShips.contains(indShips.get("Talon DD Shadow")));
	Assert.assertEquals(3, report.canActShips.size());
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon FF Surprise")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon DD Hunter")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon DD Shadow")));
	Assert.assertEquals(3, report.mustMoveShips.size());

	//impulsion F tour 2 : joueur actif Terran, actif : Thor, Caleb ; mvt : Thor, Caleb
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TERRAN.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.F.toString(), report.currentImpulse);
	Assert.assertTrue(report.canActShips.contains(indShips.get("Terran CL Thor")));
	Assert.assertTrue(report.canActShips.contains(indShips.get("Terran DD Caleb")));
	Assert.assertEquals(2, report.canActShips.size());
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Terran CL Thor")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Terran DD Caleb")));
	Assert.assertEquals(2, report.mustMoveShips.size());

	//impulsion POWER tour 1 : joueur actif Talon, aucune action ni mouvement
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.POWER.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());

	//impulsion POWER tour 2 : joueur actif Terran, aucune action ni mouvement
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TERRAN.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.POWER.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());

	//suite à la fin de tour on se retrouve dans les mêmes conditions qu'au tout début, on refait quelques tests jusqu'en impulsion B pour être sur
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.A.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TERRAN.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.A.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.B.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon FF Surprise")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon DD Hunter")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Talon DD Shadow")));
	Assert.assertEquals(3, report.mustMoveShips.size());
	report = testee.advanceImpulse();
	Assert.assertEquals(Player.TERRAN.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.B.toString(), report.currentImpulse);
	Assert.assertTrue(report.canActShips.contains(indShips.get("Terran CL Thor")));
	Assert.assertEquals(1, report.canActShips.size());
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Terran CL Thor")));
	Assert.assertTrue(report.mustMoveShips.contains(indShips.get("Terran DD Caleb")));
	Assert.assertEquals(2, report.mustMoveShips.size());
    }

    /**
     * cas particuliers où le système doit tolérer l'erreur
     */
    @Test
    public void initTestLimite()
    {
	//advanceImpulse alors que le jeu n'est pas commencé : même effet que démarrer la partie.
	GameManager.ImpulseReport report = testee.advanceImpulse();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.A.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());

	//startGame alors que le jeu est déjà démarré : aucun effet
	report = testee.startGame();
	Assert.assertEquals(Player.TALON.toString(), report.currentPlayer);
	Assert.assertEquals(GameManager.Impulse.A.toString(), report.currentImpulse);
	Assert.assertEquals(0, report.canActShips.size());
	Assert.assertEquals(0, report.mustMoveShips.size());
    }

    @Test
    public void moveTest() throws SQLException
    {
	testee.startGame();
	testee.advanceImpulse();
	testee.advanceImpulse();

	//on est désormais au premier tour de l'impulsion B, Talon doit jouer donc la Surprise est active
	int shipTestId = 684674651;
	Ship shipTest = null;

	for (Ship ship : testee.getAllShips().get(Player.TALON))
	{
	    if ("Talon FF Surprise".equals(ship.toString()))
	    {
		shipTestId = ship.getId();
		shipTest = ship;
	    }
	}

	//Surprise active, elle peut donc avancer
	GameManager.ImpulseReport report = testee.moveShipStraight(shipTestId);
	MapObject.HexCoordinates coordTest = shipTest.getCoordinates();
	MapObject.HexCoordinates coordExpected = new MapObject.HexCoordinates(9, 11, MapObject.Orientation.NE);
	Assert.assertEquals(coordExpected, coordTest);
	//Surprise a déjà bougé, on ne peut plus la bouger
	Assert.assertFalse(report.mustMoveShips.contains(shipTestId));
	testee.moveShipStraight(shipTestId);
	coordTest = shipTest.getCoordinates();
	Assert.assertEquals(coordExpected, coordTest);

	//on avance un coup pour faire tourner un vaisseau terrien
	testee.advanceImpulse();

	for (Ship ship : testee.getAllShips().get(Player.TERRAN))
	{
	    if ("Terran DD Caleb".equals(ship.toString()))
	    {
		shipTestId = ship.getId();
		shipTest = ship;
	    }
	}

	//Caleb actif, on effectue un virage illégal qui sera ignoré et il restera donc actif derrière
	testee.turnShip(shipTestId, MapObject.Orientation.NE);
	coordTest = shipTest.getCoordinates();
	coordExpected = new MapObject.HexCoordinates(0, 8, MapObject.Orientation.SW);
	Assert.assertEquals(coordExpected, coordTest);
	//Caleb actif, on le fait tourner
	report = testee.turnShip(shipTestId, MapObject.Orientation.SE);
	coordTest = shipTest.getCoordinates();
	coordExpected = new MapObject.HexCoordinates(1, 9, MapObject.Orientation.SE);
	Assert.assertEquals(coordExpected, coordTest);
	//Caleb a déjà bougé, on ne peut plus le bouger
	Assert.assertFalse(report.mustMoveShips.contains(shipTestId));
	testee.moveShipStraight(shipTestId);
	coordTest = shipTest.getCoordinates();
	Assert.assertEquals(coordExpected, coordTest);
    }
}
