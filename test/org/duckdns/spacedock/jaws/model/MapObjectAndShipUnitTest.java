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
import org.duckdns.spacedock.jaws.model.Ship.PowerCurve;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * Cette classe teste à la fois la classe Ship et sa superclasse MapObject, dont
 * les méthodes et sont appelées par Ship
 *
 * @author ykonoclast
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
	{
	    MapObject.class,
	    MapObjectDao.class,
	})
public class MapObjectAndShipUnitTest
{

    private MapObjectDao daoMock = daoMock = PowerMockito.mock(MapObjectDao.class);
    private final PowerCurve curveTest = new PowerCurve(77, 13, 3);
    private final MapObject.HexCoordinates coordTest = new MapObject.HexCoordinates(5, 9, org.duckdns.spacedock.jaws.model.MapObject.Orientation.SW);

    /**
     * teste que les bonnes méthodes des bons objets sont appelées lors de la
     * création et que les informations récupérées à ce moment sont bien celles
     * retournées par les getters
     *
     * @throws java.io.FileNotFoundException
     */
    @Test
    public void initialValuesandGettersTest() throws FileNotFoundException
    {
	PowerMockito.mockStatic(MapObjectDao.class);
	when(MapObjectDao.getInstance()).thenReturn(daoMock);
	when(daoMock.makeId()).thenReturn(4);
	when(daoMock.getPowerCurve("typetest")).thenReturn(curveTest);

	Ship shipTest = new Ship("typetest", "vaisseautest", coordTest);

	Assert.assertEquals("typetest vaisseautest", shipTest.toString());
	Assert.assertEquals(4, shipTest.getId());
	Assert.assertEquals(curveTest, shipTest.getPowerCurve());
	Assert.assertEquals(coordTest, shipTest.getCoordinates());
    }

    /**
     * teste à la fois le equals de la superclasse et celui de Ship
     *
     * @throws FileNotFoundException
     */
    @Test
    public void shipEqualsTest() throws FileNotFoundException
    {
	PowerMockito.mockStatic(MapObjectDao.class);
	when(MapObjectDao.getInstance()).thenReturn(daoMock);

	//même caracs mais l'ID est différent : ces vaisseaux sont égaux, même si pas identiques
	when(daoMock.makeId()).thenReturn(4);
	when(daoMock.getPowerCurve("typetest")).thenReturn(curveTest);

	Ship shipTest = new Ship("typetest", "vaisseautest", coordTest);

	when(daoMock.makeId()).thenReturn(5);
	Ship shipTest2 = new Ship("typetest", "vaisseautest", coordTest);

	Assert.assertEquals(shipTest, shipTest2);

	when(daoMock.makeId()).thenReturn(4);//on remet les ID égaux pour ne pas cacher d'éventuel bug dans les caracs différentes plus tard

	//une carac de différence : PowerCurve
	PowerCurve curveTest2 = new PowerCurve(5, 2, 0);
	when(daoMock.getPowerCurve("typetest")).thenReturn(curveTest2);
	shipTest2 = new Ship("typetest", "vaisseautest", coordTest);
	Assert.assertFalse(shipTest.equals(shipTest2));
	when(daoMock.getPowerCurve("typetest")).thenReturn(curveTest);//on remet les curve égales

	//une carac de différence : coordonnées
	MapObject.HexCoordinates coordTest2 = new MapObject.HexCoordinates(6, 7, org.duckdns.spacedock.jaws.model.MapObject.Orientation.NW);
	shipTest2 = new Ship("typetest", "vaisseautest", coordTest2);
	Assert.assertFalse(shipTest.equals(shipTest2));

	//une carac de différence : nom
	shipTest2 = new Ship("typetest", "vaisseautest2", coordTest);
	Assert.assertFalse(shipTest.equals(shipTest2));
    }

    @Test
    public void hexCoordinatesEqualsTest()
    {
	//égales
	MapObject.HexCoordinates coordTest2 = new MapObject.HexCoordinates(5, 9, org.duckdns.spacedock.jaws.model.MapObject.Orientation.SW);
	assertEquals(coordTest, coordTest2);

	//une carac de différence : x
	coordTest2 = new MapObject.HexCoordinates(1, 9, org.duckdns.spacedock.jaws.model.MapObject.Orientation.SW);
	Assert.assertFalse(coordTest.equals(coordTest2));

	//une carac de différence : y
	coordTest2 = new MapObject.HexCoordinates(5, 1, org.duckdns.spacedock.jaws.model.MapObject.Orientation.SW);
	Assert.assertFalse(coordTest.equals(coordTest2));

	//une carac de différence : orientation
	coordTest2 = new MapObject.HexCoordinates(5, 9, org.duckdns.spacedock.jaws.model.MapObject.Orientation.NE);
	Assert.assertFalse(coordTest.equals(coordTest2));
    }

    @Test
    public void PowerCurveEqualsTest()
    {
	//égales
	PowerCurve curveTest2 = new PowerCurve(77, 13, 3);
	assertEquals(curveTest, curveTest2);

	//une carac de différence : power
	curveTest2 = new PowerCurve(1, 13, 3);
	Assert.assertFalse(curveTest.equals(curveTest2));

	//une carac de différence : speed
	curveTest2 = new PowerCurve(77, 1, 3);
	Assert.assertFalse(curveTest.equals(curveTest2));

	//une carac de différence : turnRadius
	curveTest2 = new PowerCurve(77, 13, 1);
	Assert.assertFalse(curveTest.equals(curveTest2));
    }

    @Test
    public void getDistancePROVISOIRETest()//TODO déplacer ce code de test là où il est plus utile quand le refactoring de la méthode sera achevé
    {
	MapObject.HexCoordinates coordA = new MapObject.HexCoordinates(0, 0);
	MapObject.HexCoordinates coordB = new MapObject.HexCoordinates(0, 0);
	assertEquals(0, MapObject.getDistancePROVISOIRE(coordA, coordB));

	coordA = new MapObject.HexCoordinates(-1, 2);
	coordB = new MapObject.HexCoordinates(3, 0);
	assertEquals(6, MapObject.getDistancePROVISOIRE(coordA, coordB));

	coordA = new MapObject.HexCoordinates(3, 0);
	coordB = new MapObject.HexCoordinates(2, 2);
	assertEquals(3, MapObject.getDistancePROVISOIRE(coordA, coordB));

	coordA = new MapObject.HexCoordinates(-1, 1);
	coordB = new MapObject.HexCoordinates(3, 1);
	assertEquals(4, MapObject.getDistancePROVISOIRE(coordA, coordB));

	coordA = new MapObject.HexCoordinates(2, 2);
	coordB = new MapObject.HexCoordinates(-1, -1);
	assertEquals(3, MapObject.getDistancePROVISOIRE(coordA, coordB));

	coordA = new MapObject.HexCoordinates(1, 2);
	coordB = new MapObject.HexCoordinates(0, 0);
	assertEquals(2, MapObject.getDistancePROVISOIRE(coordA, coordB));

	coordA = new MapObject.HexCoordinates(0, -1);
	coordB = new MapObject.HexCoordinates(1, 0);
	assertEquals(1, MapObject.getDistancePROVISOIRE(coordA, coordB));

	coordA = new MapObject.HexCoordinates(1, 0);
	coordB = new MapObject.HexCoordinates(0, -1);
	assertEquals(1, MapObject.getDistancePROVISOIRE(coordA, coordB));

	coordA = new MapObject.HexCoordinates(0, 0);
	coordB = new MapObject.HexCoordinates(10, 19);
	assertEquals(19, MapObject.getDistancePROVISOIRE(coordA, coordB));
    }

    /**
     * teste mouvement et cycle sur l'enum d'orientation, les deux sont liés
     *
     * @throws java.io.FileNotFoundException
     */
    @Test
    public void moveAndOrientationCycleTestNominal() throws FileNotFoundException//todo enrichir avec les marqueurs virage
    {
	//cycle de l'orientation
	assertEquals(MapObject.Orientation.NW, MapObject.Orientation.NE.previous());
	assertEquals(MapObject.Orientation.NE, MapObject.Orientation.NW.next());

	assertEquals(MapObject.Orientation.W, MapObject.Orientation.NW.previous());
	assertEquals(MapObject.Orientation.E, MapObject.Orientation.NE.next());

	//mouvements
	PowerMockito.mockStatic(MapObjectDao.class);
	when(MapObjectDao.getInstance()).thenReturn(daoMock);
	when(daoMock.makeId()).thenReturn(4);
	when(daoMock.getPowerCurve("typetest")).thenReturn(curveTest);

	Ship shipTest = new Ship("typetest", "vaisseautest", coordTest);//position initiale {5,9,SW}

	//mouvement en spirale, vers la droite, toutes orientations
	MapObject.HexCoordinates coordExpected = new MapObject.HexCoordinates(6, 9, MapObject.Orientation.SW);
	shipTest.moveStraight();
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(6, 8, MapObject.Orientation.W);
	shipTest.turn(MapObject.Orientation.W);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(6, 7, MapObject.Orientation.W);
	shipTest.moveStraight();
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(5, 6, MapObject.Orientation.NW);
	shipTest.turn(MapObject.Orientation.NW);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(4, 5, MapObject.Orientation.NW);
	shipTest.moveStraight();
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(3, 5, MapObject.Orientation.NE);
	shipTest.turn(MapObject.Orientation.NE);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(2, 5, MapObject.Orientation.NE);
	shipTest.moveStraight();
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(2, 6, MapObject.Orientation.E);
	shipTest.turn(MapObject.Orientation.E);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(2, 7, MapObject.Orientation.E);
	shipTest.moveStraight();
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(3, 8, MapObject.Orientation.SE);
	shipTest.turn(MapObject.Orientation.SE);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(4, 9, MapObject.Orientation.SE);
	shipTest.moveStraight();
	assertEquals(coordExpected, shipTest.getCoordinates());

	//mouvement en spirale vers la gauche, toutes orientations
	coordExpected = new MapObject.HexCoordinates(4, 10, MapObject.Orientation.E);
	shipTest.turn(MapObject.Orientation.E);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(4, 11, MapObject.Orientation.E);
	shipTest.moveStraight();
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(3, 11, MapObject.Orientation.NE);
	shipTest.turn(MapObject.Orientation.NE);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(2, 11, MapObject.Orientation.NE);
	shipTest.moveStraight();
	assertEquals(coordExpected, shipTest.getCoordinates());

	//toujours sur la gauche mais plus de mouvement tout droit : on sait que cela marche
	coordExpected = new MapObject.HexCoordinates(1, 10, MapObject.Orientation.NW);
	shipTest.turn(MapObject.Orientation.NW);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(1, 9, MapObject.Orientation.W);
	shipTest.turn(MapObject.Orientation.W);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(2, 9, MapObject.Orientation.SW);
	shipTest.turn(MapObject.Orientation.SW);
	assertEquals(coordExpected, shipTest.getCoordinates());

	coordExpected = new MapObject.HexCoordinates(3, 10, MapObject.Orientation.SE);
	shipTest.turn(MapObject.Orientation.SE);
	assertEquals(coordExpected, shipTest.getCoordinates());
    }

    /**
     * teste les cas où le vaisseau ne tourne pas
     *
     * @throws FileNotFoundException
     */
    @Test
    public void turnTestLimite() throws FileNotFoundException
    {
	PowerMockito.mockStatic(MapObjectDao.class);
	when(MapObjectDao.getInstance()).thenReturn(daoMock);
	when(daoMock.makeId()).thenReturn(4);
	when(daoMock.getPowerCurve("typetest")).thenReturn(curveTest);

	Ship shipTest = new Ship("typetest", "vaisseautest", coordTest);//position initiale {5,9,SW}

	shipTest.turn(MapObject.Orientation.SW);
	assertEquals(coordTest, shipTest.getCoordinates());

	shipTest.turn(MapObject.Orientation.NW);
	assertEquals(coordTest, shipTest.getCoordinates());

	shipTest.turn(MapObject.Orientation.NE);
	assertEquals(coordTest, shipTest.getCoordinates());

	shipTest.turn(MapObject.Orientation.E);
	assertEquals(coordTest, shipTest.getCoordinates());
    }

    /**
     *
     * @throws FileNotFoundException
     */
    @Test
    public void canTurnTestNominal() throws FileNotFoundException
    {
	PowerMockito.mockStatic(MapObjectDao.class);
	when(MapObjectDao.getInstance()).thenReturn(daoMock);
	when(daoMock.makeId()).thenReturn(4);
	when(daoMock.getPowerCurve("typetest")).thenReturn(curveTest);

	Ship shipTest = new Ship("typetest", "vaisseautest", coordTest);//position initiale {5,9,SW}

	Assert.assertTrue(shipTest.canTurn(MapObject.Orientation.W));
	Assert.assertTrue(shipTest.canTurn(MapObject.Orientation.SE));

	Assert.assertFalse(shipTest.canTurn(MapObject.Orientation.NW));
	Assert.assertFalse(shipTest.canTurn(MapObject.Orientation.NE));
	Assert.assertFalse(shipTest.canTurn(MapObject.Orientation.E));
	Assert.assertFalse(shipTest.canTurn(MapObject.Orientation.SW));
    }
}
