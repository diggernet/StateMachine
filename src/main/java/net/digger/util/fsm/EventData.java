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
 * Class to hold the Action to take and/or State to transition to for an Event.
 * 
 * @author walton
 *
 * @param <A> Type of Action enum.
 * @param <S> Type of State enum.
 */
public class EventData<A extends Enum<?>, S extends Enum<?>> {
	/**
	 * The Action to take for this Event.
	 * Null means do nothing.
	 */
	public final A action;
	
	/**
	 * The State to transition to for this Event.
	 * Null means stay in the current State.
	 */
	public final S state;

	/**
	 * Create a new EventData instance.
	 * 
	 * @param action Action to take for this Event.
	 * @param state State to transition to for this Event.
	 */
	public EventData(A action, S state) {
		this.action = action;
		this.state = state;
	}
}
