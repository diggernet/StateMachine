/**
 * Copyright © 2018  David Walton
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
 * Interface used by StateMachine.getEventData when looking up data for an Event.
 * <p>
 * An implementation of this interface can be used to map Events to other Events, for lookup purposes only.
 * For example, this could map a numeric Event which is outside the allowed range to another value inside the range.
 * 
 * @author walton
 * 
 * @param <E> Type of Event objects.
 */
public interface EventMapper<E> {
	/**
	 * Map an Event to another Event to be used for lookup.
	 * 
	 * @param event Event value to map.
	 * @return Event value to use for lookup.
	 */
	public E map(E event);
}
