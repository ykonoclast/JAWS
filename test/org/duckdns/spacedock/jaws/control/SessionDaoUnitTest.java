/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duckdns.spacedock.jaws.control;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.duckdns.spacedock.jaws.model.MapObject;
import org.duckdns.spacedock.jaws.model.Ship;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ykonoclast
 */
public class SessionDaoUnitTest
{

    private SessionDao testee;

    @Before
    public void setUpforEach() throws FileNotFoundException
    {
	testee = SessionDao.getInstance();
    }

    /**
     * Teste aussi la méthode privée makeShip qui n'est appelée que par
     * loadScenario (et n'existe que pour limiter la duplication de code)
     */
    @Test
    public void loadScenarioTestNominal() throws FileNotFoundException
    {
	List<Ship> terranShipsActual = new ArrayList<>();
	List<Ship> terranShipsExpected = new ArrayList<>();
	terranShipsExpected.add(new Ship("Terran CL", "Thor", new MapObject.HexCoordinates(0, 6, MapObject.Orientation.SW)));
	terranShipsExpected.add(new Ship("Terran DD", "Caleb", new MapObject.HexCoordinates(0, 8, MapObject.Orientation.SW)));

	List<Ship> talonShipsActual = new ArrayList<>();
	List<Ship> talonShipsExpected = new ArrayList<>();
	talonShipsExpected.add(new Ship("Talon FF", "Surprise", new MapObject.HexCoordinates(10, 11, MapObject.Orientation.NE)));
	talonShipsExpected.add(new Ship("Talon DD", "Hunter", new MapObject.HexCoordinates(10, 12, MapObject.Orientation.NE)));
	talonShipsExpected.add(new Ship("Talon DD", "Shadow", new MapObject.HexCoordinates(10, 13, MapObject.Orientation.NE)));

	testee.loadScenario("scenar2", talonShipsActual, terranShipsActual);

	//assertTrue(terranShipsActual.equals(terranShipsExpected));
	assertEquals(terranShipsActual, terranShipsExpected);
	//assertTrue(talonShipsActual.equals(talonShipsExpected));
	assertEquals(talonShipsActual, talonShipsExpected);
    }

    /**
     * Teste aussi la méthode privée makeShip qui n'est appelée que par
     * loadScenario (et n'existe que pour limiter la duplication de code)
     */
    @Test
    public void loadScenarioTestLimite() throws FileNotFoundException
    {
	//test de l'orientation NE par défaut si erreur dans le JSON
	List<Ship> terranShipsActual = new ArrayList<>();
	List<Ship> terranShipsExpected = new ArrayList<>();
	terranShipsExpected.add(new Ship("Terran CL", "Thor", new MapObject.HexCoordinates(1, 10, MapObject.Orientation.NE)));

	List<Ship> talonShipsActual = new ArrayList<>();
	List<Ship> talonShipsExpected = new ArrayList<>();
	talonShipsExpected.add(new Ship("Talon FF", "Surprise", new MapObject.HexCoordinates(6, 0, MapObject.Orientation.NE)));

	testee.loadScenario("testOrientationNE", talonShipsActual, terranShipsActual);

	assertTrue(terranShipsActual.equals(terranShipsExpected));
	assertTrue(talonShipsActual.equals(talonShipsExpected));
    }

    /**
     * chargement correct du fichier JSON de configuration (en vérité effectué
     * dans le constructeur mais celui-ci est privé et c'ést bien cette méthode
     * qui est l'interface de la classe pour cela)
     *
     * @throws FileNotFoundException
     */
    @Test
    public void getCurveByImpulseTestNominal() throws FileNotFoundException
    {
	List<Integer> result = testee.getCurveByImpulse(GameManager.Impulse.A);
	List<Integer> expected = new ArrayList<>(Arrays.asList(4, 5, 6));
	Assert.assertEquals(expected, result);

	result = testee.getCurveByImpulse(GameManager.Impulse.B);
	expected = new ArrayList<>(Arrays.asList(3, 5, 6));
	Assert.assertEquals(expected, result);

	result = testee.getCurveByImpulse(GameManager.Impulse.C);
	expected = new ArrayList<>(Arrays.asList(2, 4, 5, 6));
	Assert.assertEquals(expected, result);

	result = testee.getCurveByImpulse(GameManager.Impulse.D);
	expected = new ArrayList<>(Arrays.asList(3, 4, 5, 6));
	Assert.assertEquals(expected, result);

	result = testee.getCurveByImpulse(GameManager.Impulse.E);
	expected = new ArrayList<>(Arrays.asList(6));
	Assert.assertEquals(expected, result);

	result = testee.getCurveByImpulse(GameManager.Impulse.F);
	expected = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
	Assert.assertEquals(expected, result);

    }

}
