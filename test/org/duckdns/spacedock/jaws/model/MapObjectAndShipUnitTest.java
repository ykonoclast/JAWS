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
    private PowerCurve curveTest = new PowerCurve(77, 13, 3);
    private MapObject.HexCoordinates coordTest = new MapObject.HexCoordinates(5, 9, org.duckdns.spacedock.jaws.model.MapObject.Orientation.SW);

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
}
