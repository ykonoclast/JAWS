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
 * classe représentant spécifiquement un vaisseau spatial
 *
 * @author ykonoclast
 */
public class Ship extends MapObject
{

    private PowerCurve m_powerCurve;

    /**
     * constructeur, appelle la superclasse puis enrichit lui-même ses donnés
     * pour ce qui est propre aux vaisseaux
     *
     * @param p_type typage au sens JSON
     * @param p_name pour affichage
     * @param p_coordinates position initiale
     * @throws java.io.FileNotFoundException
     *
     */
    public Ship(String p_type, String p_name, MapObject.HexCoordinates p_coordinates) throws FileNotFoundException
    {
	super(p_type, p_name, p_coordinates);
	m_powerCurve = m_mapObjectDao.getPowerCurve(p_type);
    }

    /**
     * avance un cran dans la même direction
     */
    public void moveStraight()
    {
	transformPos(getCoordinates().orientation);
    }

    /**
     * avance d'un cran et modifie l'orientation du vaisseau dans la direction
     * donnée
     *
     * @param p_orientation
     */
    public void turn(Orientation p_orientation)
    {
	if (canTurn(p_orientation))//tolérance d'erreur : rejet silencieux de l'ordre si invalide
	{
	    transformPos(p_orientation);
	}
    }

    /**
     * effectue la transofrmation de la position du vaisseau en fonction de
     * l'orientation fournie en paramétre (avance de 1 hex dans cette direction)
     *
     * @param p_orienOrientation
     */
    private void transformPos(Orientation p_orientation)
    {
	int curL = m_coordinates.posL;
	int curC = m_coordinates.posC;
	switch (p_orientation)
	{
	    case NE:
		m_coordinates = new HexCoordinates(curL - 1, curC, p_orientation);
		break;
	    case E:
		m_coordinates = new HexCoordinates(curL, curC + 1, p_orientation);
		break;
	    case SE:
		m_coordinates = new HexCoordinates(curL + 1, curC + 1, p_orientation);
		break;
	    case SW:
		m_coordinates = new HexCoordinates(curL + 1, curC, p_orientation);
		break;
	    case W:
		m_coordinates = new HexCoordinates(curL, curC - 1, p_orientation);
		break;
	    case NW:
		m_coordinates = new HexCoordinates(curL - 1, curC - 1, p_orientation);
		break;
	}
    }

    /**
     *
     * @param p_orientation
     * @return vrai si le vaisseau peut actuellement effectuer un virage dans la
     * direction indiquée (uniquement les deux bords adjacents à sa direction
     * actuelle)
     */
    public boolean canTurn(Orientation p_orientation)
    {
	boolean result = false;
	if (m_coordinates.orientation.equals(p_orientation.next()) || m_coordinates.orientation.equals(p_orientation.previous()))
	{
	    //TODO ajouter gestion des marqueurs virages
	    result = true;
	}

	return result;
    }

    /**
     *
     * @return
     */
    public PowerCurve getPowerCurve()
    {
	return m_powerCurve;//on peut renvoyer la PowerCurve sans crainte de modification : c'est un objet imutable de toute façon
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
		if (super.equals(o) && getPowerCurve().equals(((Ship) o).getPowerCurve()))//seulement les éléments en plus de ceux présents en superclasse
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
	return Objects.hash(super.hashCode(), m_powerCurve);//seulement les éléments en plus de ceux présents en superclasse
    }

    /**
     * classe encapsulant une powercurve de vaisseau (les trois
     * caractéristiques)
     */
    public static class PowerCurve
    {

	/**
	 * énergie
	 */
	public final int power;

	/**
	 * vitesse
	 */
	public final int speed;

	/**
	 * rayon de virage
	 */
	public final int turnRadius;

	/**
	 * constructeur
	 *
	 * @param p_power
	 * @param p_speed
	 * @param p_turnRadius
	 */
	public PowerCurve(int p_power, int p_speed, int p_turnRadius)
	{
	    power = p_power;
	    speed = p_speed;
	    turnRadius = p_turnRadius;
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
		    if (power == ((PowerCurve) o).power && speed == ((PowerCurve) o).speed && turnRadius == ((PowerCurve) o).turnRadius)
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
	    return Objects.hash(power, speed, turnRadius);
	}
    }
}
