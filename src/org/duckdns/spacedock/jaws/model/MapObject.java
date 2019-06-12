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
import java.util.Objects;

/**
 * superclasse de tous les objets présents sur le plateau de jeu, contient les
 * primitives de gestion des coordonnées hexagonales, abstract car ne devrait
 * pas pouvoir être instanciée : elle ne représente rien de concret
 *
 * @author ykonoclast
 */
public abstract class MapObject
{

    /**
     * identifiant unique de l'objet
     */
    final int m_id;

    /**
     * nom de l'objet, pour affichage
     */
    final String m_name;

    /**
     * coordonnés sur l'hexmap
     */
    final MapObject.HexCoordinates m_coordinates;

    /**
     * type de l'objet, pour affichage et récupération des caracs dans la
     * référence
     */
    final String m_type;

    /**
     * DAO d'accès aux fichiers de configuration pour créer des objets
     */
    final MapObjectDao m_mapObjectDao;

    /**
     * constructeur, accès package uniquement, ce sont les sous-classes qui
     * l'appelleront
     *
     * @param p_type utilisé pour récupération des caracs dans le JSON
     * @param p_name pour affichage
     * @param p_coordinates coordonnées initiales
     */
    MapObject(String p_type, String p_name, MapObject.HexCoordinates p_coordinates) throws FileNotFoundException
    {
	m_mapObjectDao = MapObjectDao.getInstance();
	m_type = p_type;
	m_name = p_name;
	m_id = m_mapObjectDao.makeId();
	m_coordinates = p_coordinates;
    }

    /**
     *
     * @return
     */
    public int getId()
    {
	return m_id;
    }

    /**
     *
     * @return une copie des coordonnées de cet objet
     */
    public MapObject.HexCoordinates getCoordinates()
    {
	return new MapObject.HexCoordinates(m_coordinates.posX, m_coordinates.posY, m_coordinates.orientation);//copie, pas de possibilité de modifier la position de l'éxtérieur
    }

    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
	return (m_type + " " + m_name);//TODO remplacer cet emploi direct du type par une recherche dans les Strings
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)//TODO : mettre à jour au fur et à mesure que la classe est updatée
    {
	boolean result = false;

	if (this == o)
	{
	    result = true;
	}
	else
	{
	    if (o != null && getClass() == o.getClass())
	    {
		if (toString().equals(o.toString()) && getCoordinates().equals(((Ship) o).getCoordinates()))//l'ID n'est PAS considéré : il constitue une identité, pas une égalité
		{
		    result = true;

		}
	    }
	}
	return result;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode()//TODO : mettre à jour au fur et à mesure que la classe est updatée
    {
	return Objects.hash(toString(), m_coordinates);
    }

    /**
     * enum des directions possibles pour l'orientation d'un objet
     */
    public enum Orientation
    {
	NE, E, SE, SW, W, NW
    }

    /**
     * coordonnées hexagonales, les axes sont à 45° et l'orientation est
     * considérée
     */
    public static class HexCoordinates
    {

	public final int posX;
	public final int posY;
	public final Orientation orientation;

	public HexCoordinates(int p_posX, int p_posY, Orientation p_orientation)
	{
	    posX = p_posX;
	    posY = p_posY;
	    orientation = p_orientation;
	}

	/**
	 *
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o)
	{
	    boolean result = false;

	    if (this == o)
	    {
		result = true;
	    }
	    else
	    {
		if (o != null && getClass() == o.getClass())
		{
		    if (posX == ((HexCoordinates) o).posX && posY == ((HexCoordinates) o).posY && orientation == ((HexCoordinates) o).orientation)
		    {
			result = true;
		    }
		}
	    }
	    return result;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int hashCode()
	{
	    return Objects.hash(posX, posY, orientation);
	}
    }
}
