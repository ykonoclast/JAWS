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
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ykonoclast
 */
public class MapObjectDaoUnitTest
{

    private MapObjectDao testee;

    @Before
    public void setUpforEach() throws FileNotFoundException
    {
	testee = MapObjectDao.getInstance();
    }

    @Test
    public void getPowerCurveTestNominal()
    {
	//CL Terrien
	Ship.PowerCurve expected = new Ship.PowerCurve(3, 3, 1);
	Ship.PowerCurve actual = testee.getPowerCurve("Terran CL");
	Assert.assertEquals(expected, actual);

	//DD Terrien
	expected = new Ship.PowerCurve(2, 3, 1);
	actual = testee.getPowerCurve("Terran DD");
	Assert.assertEquals(expected, actual);

	//FF Talon
	expected = new Ship.PowerCurve(2, 3, 0);
	actual = testee.getPowerCurve("Talon FF");
	Assert.assertEquals(expected, actual);

	//DD Talon
	expected = new Ship.PowerCurve(1, 3, 0);
	actual = testee.getPowerCurve("Talon DD");
	Assert.assertEquals(expected, actual);

    }

    @Test
    public void getPowerCurveTestErreur()
    {
	//type non existant
	try
	{
	    testee.getPowerCurve("USS Enterprise");
	    fail();
	}
	catch (IllegalArgumentException e)
	{
	    Assert.assertEquals("paramétre aberrant:ce type de vaisseau n'existe pas: USS Enterprise", e.getMessage());
	}
    }

    /**
     * crée un grand nombre d'ID et vérifie leur unicité
     */
    @Test
    public void makeIdTest()
    {

	List<Integer> list = new ArrayList<>();
	for (int i = 0; i < 100000; ++i)
	{
	    int currentId = testee.makeId();
	    Assert.assertFalse(list.contains(currentId));
	    list.add(currentId);
	}
    }
}
