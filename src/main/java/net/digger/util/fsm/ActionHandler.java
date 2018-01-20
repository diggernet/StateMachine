/**
 * Copyright © 2017-2018  David Walton
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.digger.util.fsm;

/**
 * Interface to implement for handling state machine events and transitions.
 * 
 * @author walton
 *
 * @param <A> Type of Action enum.
 * @param <S> Type of State enum.
 * @param <E> Type of Event objects.
 */
public interface ActionHandler<A extends Enum<?>, S extends Enum<?>, E> {
	/**
	 * Called when entering a State, if onEntry Action is set.
	 * 
	 * @param state State being entered.
	 * @param action Action to perform.
	 */
	public void onEntry(S state, A action);

	/**
	 * Called when an Event triggers an Action.
	 * 
	 * @param state Current State.
	 * @param event Event being handled.
	 * @param action Action triggered.
	 */
	public void onEvent(S state, E event, A action);
	
	/**
	 * Called when exiting a State, if onExit Action is set.
	 * 
	 * @param state State being exited.
	 * @param action Action to perform.
	 */
	public void onExit(S state, A action);
}
