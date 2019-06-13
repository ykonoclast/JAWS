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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.duckdns.spacedock.jaws.model.MapObject;
import org.duckdns.spacedock.jaws.model.Ship;
import org.duckdns.spacedock.jaws.model.Ship.PowerCurve;

/**
 * classe gérant les opérations relatives à la partie en cours : initialisation,
 * gestion des tours, des vaisseaux en jeu et interface entre l'UI et les objets
 * métiers.
 *
 * @author ykonoclast
 */
public class SessionManager
{

    /**
     * ensemble des vaisseaux en jeu, classés par joueur
     */
    private final Map<Player, List<Ship>> m_listShips = new EnumMap<>(Player.class);

    /**
     * liste des identifiants des vaisseaux pouvant agir durant le tour
     * d'impulsion en cours
     */
    private final ArrayList<Integer> m_canActShips = new ArrayList<>();

    /**
     * liste des identifiants des vaisseaux devant bouger durant le tour
     * d'impulsion en cours
     */
    private final ArrayList<Integer> m_mustMoveShips = new ArrayList<>();

    /**
     * DAO d'accès aux éléments de "partie" : scénarios, sauvegarde du jeu en
     * cours etc.
     */
    private final SessionDao m_sessionDao;

    /**
     * impulse actuelle
     */
    private Impulse m_currentImpulse;

    /**
     * vrai si l'on est en train de jouer le tour du premier joueur dans
     * l'impulsion actuelle
     */
    boolean m_firstPlayerTurn;

    /**
     * vrai si la partie a bien débuté
     */
    boolean m_gameStarted = false;

    /**
     * joueur ayant actuellement l'initiative
     */
    Player m_initHolder;

    /**
     * joueur dont c'est actuellement le tour
     */
    Player m_currentPlayer;

    /**
     * constructeur, l'objet ne peut-être créé que POUR un scénario donné
     *
     * @param p_Scenario le scénario a charger depuis les fichiers JSON de
     * référence
     * @throws FileNotFoundException
     */
    SessionManager(String p_Scenario) throws FileNotFoundException
    {
	m_sessionDao = SessionDao.getInstance();
	List<Ship> listTalonShips = new ArrayList<>();
	List<Ship> listTerranShips = new ArrayList<>();

	m_sessionDao.loadScenario(p_Scenario, listTalonShips, listTerranShips);

	m_listShips.put(Player.TALON, listTalonShips);
	m_listShips.put(Player.TERRAN, listTerranShips);

	m_initHolder = Player.TALON;//par défaut au début de la plupart des scénarii
    }

    ImpulseReport startGame()
    {
	if (!m_gameStarted)
	{//sécurité : tous les appels après le premier seront ignorés
	    m_currentImpulse = Impulse.A;
	    m_firstPlayerTurn = true;
	    m_currentPlayer = m_initHolder;//défini dans le constructeur par le scénario
	    m_gameStarted = true;
	}
	return makeImpulseReport();
    }

    ImpulseReport advanceImpulse()
    {
	//TODO vérifier que tous les mouvements ont été exécutés
	//TODO tracker où l'on en est du phasage interne au tour : actions, mouvements, tirs sauf pour la power phase
	//TODO pour tous les ordres vérfiier que le phasing est conforme

	ImpulseReport result;

	if (m_gameStarted)
	{
	    if (!m_firstPlayerTurn)
	    {//on était au tour du second joueur, il faut donc changer l'impulsion en plus de changer de joueur
		m_currentImpulse = m_currentImpulse.next();
	    }
	    m_firstPlayerTurn = !m_firstPlayerTurn;
	    m_currentPlayer = m_currentPlayer.next();//TODO à terme il faudra gérer le changement d'initiative par les actions

	    result = makeImpulseReport();
	}
	else
	{//afin de ne pas se retrouver dans un état indéfini où le code appelant ferait avancer l'init sans l'avoir initialisée
	    result = startGame();
	}
	return result;
    }

    /**
     *
     * @return un ImpulseReport décrivant la situation actuelle sur le plateau
     * de jeu, ATTENTION : n'utiliser qu'en début d'impulsion car sinon la liste
     * sera réinitialisée sans tenir compte des mouvements et actions effectués.
     */
    private ImpulseReport makeImpulseReport()
    {
	//reset des listes d'action et de mouvement pour le tour
	m_canActShips.clear();
	m_mustMoveShips.clear();

	if (m_currentImpulse != Impulse.POWER)
	{
	    m_listShips.get(m_currentPlayer).forEach((ship) ->
	    {
		PowerCurve curve = ship.getPowerCurve();
		if (m_sessionDao.getCurveByImpulse(m_currentImpulse).contains(curve.power))
		{//ce vaisseau peut agir durant ce tour
		    m_canActShips.add(ship.getId());
		}
		if (m_sessionDao.getCurveByImpulse(m_currentImpulse).contains(curve.speed))
		{
		    //ce vaisseau doit bouger durant ce tour
		    m_mustMoveShips.add(ship.getId());
		}
	    });
	}
	else
	{
	    //TODO traiter ce cas
	}
	return updateImpulseReport();
    }

    /**
     *
     * @return la mise à jour du rapport sans modification des listes de
     * vaisseaux actifs
     */
    private ImpulseReport updateImpulseReport()
    {
	return new ImpulseReport(m_currentPlayer.toString(), m_canActShips, m_mustMoveShips, m_currentImpulse.toString());
    }

    /**
     * fait avancer un vaisseau tout droit et met à jour le rapport d'impulsion
     *
     * @param p_shipId
     */
    ImpulseReport moveShipStraight(int p_shipId)
    {
	Ship ship = getShipToMove(p_shipId);
	if (ship != null)
	{
	    ship.moveStraight();
	}

	return updateImpulseReport();
    }

    /**
     * fait tourner un vaisseau et met à jour le rapport d'impulsion
     *
     * @param p_shipId
     * @param p_orientation
     */
    ImpulseReport turnShip(int p_shipId, MapObject.Orientation p_orientation)
    {
	Ship ship = getShipToMove(p_shipId);
	if (ship != null)
	{
	    ship.turn(p_orientation);
	}

	return updateImpulseReport();
    }

    /**
     * vérifie si un vaisseau existe et peut bouger et le renvoie en fonction de
     * son identifiant
     *
     * @param p_shipId
     * @return null si aucun vaisseau ne correspond aux conditions
     */
    private Ship getShipToMove(int p_shipId)
    {
	Ship result = null;
	if (m_mustMoveShips.contains(p_shipId))
	{
	    for (Ship ship : m_listShips.get(m_currentPlayer))
	    {
		if (ship.getId() == p_shipId)
		{
		    result = ship;
		}
	    }
	    m_mustMoveShips.remove(Integer.valueOf(p_shipId));//le vaisseau ne peut plus bouger
	}//TODO faire quelque sinon ou juste ignorer l'ordre illégal?
	return result;
    }

    /**
     *
     * @return la liste de tous les vaisseaux en jeu
     */
    Map<Player, List<Ship>> getAllShips()
    {
	return new EnumMap<>(m_listShips);//TODO il vaudrait certainement mieux renvoyer cela comme une partie de l'ImpulseReport
    }

    /**
     * sous-classe représentant la situation au début d'un tour de joueur :
     * vaisseaux actifs, situation dans le tour de jeu etc.
     */
    static class ImpulseReport
    {//TODO voir si on passe des Strings aux vrais Enum, en fonction de comment marche le vrai serveur
	//TODO ajouter le numéro du tour
	//TODO ajouter si on est au tour du joueur ayant l'init ou pas

	/**
	 * joueur dont c'est le début du tour
	 */
	public final String currentPlayer;

	/**
	 * liste des id des vaisseaux pouvant agir ce tour-ci
	 */
	public final List<Integer> canActShips;

	/**
	 * liste des id des vaisseaux devant bouger ce tour-ci
	 */
	public final List<Integer> mustMoveShips;

	/**
	 * libellé de l'impulsion en cours
	 */
	public final String currentImpulse;

	public ImpulseReport(String p_currentPlayer, List<Integer> p_canActShips, List<Integer> p_mustMoveShips, String p_currentImpulse)
	{
	    currentPlayer = p_currentPlayer;
	    canActShips = p_canActShips;
	    mustMoveShips = p_mustMoveShips;
	    currentImpulse = p_currentImpulse;

	}
    }

    /**
     * représentation d'un joueur : talon ou terrien
     */
    enum Player
    {
	TALON, TERRAN
	{
	    @Override
	    public Player next()
	    {
		return values()[0]; // retour à TALON dans le cas du dernier élément
	    }

	};

	public Player next()
	{
	    // pas besoin de checker si on est au dernier, l'override ci-dessus gère cela
	    return values()[ordinal() + 1];
	}
    }

    /**
     * liste des impulses, y compris la power phase
     */
    enum Impulse
    {
	A("A"),
	B("B"),
	C("C"),
	D("D"),
	E("E"),
	F("F"),
	POWER("Power Phase")
	{
	    @Override
	    public Impulse next()
	    {
		return values()[0]; // retour à A dans le cas du dernier élément
	    }

	};

	public Impulse next()
	{
	    // pas besoin de checker si on est au dernier, l'override ci-dessus gère cela
	    return values()[ordinal() + 1];
	}

	/**
	 * porteur du toString()
	 */
	private final String text;

	/**
	 * overrride du constructeur par défaut pour avoir le texte personnalisé
	 *
	 * @param text
	 */
	Impulse(final String text)
	{
	    this.text = text;
	}

	@Override
	public String toString()
	{
	    return text;
	}
    }
}
