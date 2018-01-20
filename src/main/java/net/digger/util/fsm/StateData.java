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

import java.util.HashMap;
import java.util.Map;

/**
 * Data for a parser state.
 * 
 * @author walton
 *
 * @param <A> Type of Action enum.
 * @param <S> Type of State enum.
 * @param <E> Type of Event objects.
 */
public class StateData<A extends Enum<?>, S extends Enum<?>, E> {
	/**
	 * Parser State this data is for.
	 */
	public final S state;

	/**
	 * Action to take when entering this State.
	 */
	public final A onEntry;

	/**
	 * Action to take when leaving this State.
	 */
	public final A onExit;

	/**
	 * Map of Events recognized by this State, and the associated EventData.
	 */
	protected final Map<E, EventData<A, S>> events = new HashMap<>();
	
	/**
	 * Minimum Event value, for Integer, Character or Enum Events.
	 */
	private final Integer minEvent;

	/**
	 * Maximum Event value, for Integer, Character or Enum Events.
	 */
	private final Integer maxEvent;
	
	/**
	 * Create a new StateData instance.
	 * 
	 * @param state Parser State this data is for, or ANYWHERE to apply to any State.
	 * @param onEntry Action to take when entering this State, or NO_ACTION.
	 * @param onExit Action to take when leaving this State, or NO_ACTION.
	 */
	public StateData(S state, A onEntry, A onExit) {
		this(state, onEntry, onExit, null, null);
	}
	
	/**
	 * Create a new StateData instance, if Event type is Integer, Character or Enum.
	 * <p>
	 * This constructor enables range checking in addEvent() and addEvents().
	 * 
	 * @param state Parser State this data is for, or ANYWHERE to apply to any State.
	 * @param onEntry Action to take when entering this State, or NO_ACTION.
	 * @param onExit Action to take when leaving this State, or NO_ACTION.
	 * @param minEvent Minimum Event value.
	 * @param maxEvent Maximum Event value.
	 * @throws IllegalArgumentException If called for Event type other than Integer, Character or Enum.
	 */
	public StateData(S state, A onEntry, A onExit, E minEvent, E maxEvent) throws IllegalArgumentException {
		this.state = state;
		this.onEntry = onEntry;
		this.onExit = onExit;
		if ((minEvent == null) && (maxEvent == null)) {
			this.minEvent = null;
			this.maxEvent = null;
			return;
		}
		E event = (minEvent == null) ? maxEvent : minEvent;
		// Integer Event
		if (event instanceof Integer) {
			if ((int)minEvent > (int)maxEvent) {
				throw new IllegalArgumentException("minEvent must be <= maxEvent");
			}
			this.minEvent = (minEvent == null) ? Integer.MIN_VALUE : (int)minEvent;
			this.maxEvent = (maxEvent == null) ? Integer.MAX_VALUE : (int)maxEvent;
			return;
		}
		// Character Event
		if (event instanceof Character) {
			if ((char)minEvent > (char)maxEvent) {
				throw new IllegalArgumentException("minEvent must be <= maxEvent");
			}
			this.minEvent = (minEvent == null) ? Character.MIN_VALUE : ((char)minEvent & 0xFFFF);
			this.maxEvent = (maxEvent == null) ? Character.MAX_VALUE : ((char)maxEvent & 0xFFFF);
			return;
		}
		// Enum Event
		if (event instanceof Enum) {
			if (((Enum<?>)minEvent).ordinal() > ((Enum<?>)maxEvent).ordinal()) {
				throw new IllegalArgumentException("minEvent must be <= maxEvent");
			}
			this.minEvent = (minEvent == null) ? 0 : ((Enum<?>)minEvent).ordinal();
			this.maxEvent = (maxEvent == null) ? minEvent.getClass().getEnumConstants().length - 1 : ((Enum<?>)maxEvent).ordinal();
			return;
		}
		throw new IllegalArgumentException("This constructor requires Integer, Character or Enum event type.");
	}
	
	/**
	 * Add an Event to this State.
	 * 
	 * @param event Event to react to.
	 * @param action Action to take on this Event, or NO_ACTION.
	 * @param state State to transition to on this Event, or NO_TRANSITION.
	 * @throws IllegalArgumentException If event is out of range, for Integer, Character or Enum Event.
	 */
	public void addEvent(E event, A action, S state) throws IllegalArgumentException {
		if ((event != null) && (minEvent != null) && (maxEvent != null)) {
			String message = null;
			if (event instanceof Integer) {
				// Integer Event
				if (((int)event < minEvent) || ((int)event > maxEvent)) {
					message = String.format("Event must be 0x%02x (%d) to 0x%02x (%d). Received: 0x%02x (%d).",
							minEvent, minEvent, maxEvent, maxEvent, (int)event, (int)event);
				}
			} else if (event instanceof Character) {
				// Character Event
				if (((char)event < minEvent) || ((char)event > maxEvent)) {
					message = String.format("Event must be 0x%02x (%c) to 0x%02x (%c). Received: 0x%02x (%c).",
							minEvent, (char)(int)minEvent, maxEvent, (char)(int)maxEvent,
							(char)event & 0xFF, event);
				}
			} else if (event instanceof Enum) {
				// Enum Event
				if ((((Enum<?>)event).ordinal() < minEvent) || (((Enum<?>)event).ordinal() > maxEvent)) {
					message = String.format("Event must be %s (%d) to %s (%d). Received: %s (%d).",
							event.getClass().getEnumConstants()[minEvent].toString(), minEvent,
							event.getClass().getEnumConstants()[maxEvent].toString(), maxEvent,
							event.toString(), ((Enum<?>)event).ordinal());
				}
			}
			if (message != null) {
				throw new IllegalArgumentException(message);
			}
		}
		events.put(event, new EventData<>(action, state));
	}
	
	/**
	 * Add a range of Events to this State.
	 * 
	 * @param first First Event to react to.
	 * @param last Last Event to react to.
	 * @param action Action to take on this Event, or NO_ACTION.
	 * @param state State to transition to on this Event, or NO_TRANSITION.
	 * @throws IllegalArgumentException If event is out of range, for Integer, Character or Enum Event.
	 */
	public void addEvents(E first, E last, A action, S state) throws IllegalArgumentException {
		if ((first == null) || (last == null)) {
			throw new IllegalArgumentException("Neither first nor last can be null.");
		}
		if ((minEvent != null) && (maxEvent != null)) {
			String message = null;
			if (first instanceof Integer) {
				// Integer Event
				if (((int)first < minEvent) || ((int)first > maxEvent) || ((int)last < minEvent) || ((int)last > maxEvent)) {
					message = String.format("Event must be 0x%02x (%d) to 0x%02x (%d). Received range: 0x%02x (%d) to 0x%02x (%d).",
							minEvent, minEvent, maxEvent, maxEvent, (int)first, (int)first, (int)last, (int)last);
				}
			} else if (first instanceof Character) {
				// Character Event
				if (((char)first < minEvent) || ((char)first > maxEvent) || ((char)last < minEvent) || ((char)last > maxEvent)) {
					message = String.format("Event must be 0x%02x (%c) to 0x%02x (%c). Received range: 0x%02x (%c) to 0x%02x (%c).",
							minEvent, (char)(int)minEvent, maxEvent, (char)(int)maxEvent,
							(char)first & 0xFF, first, (char)last & 0xFF, last);
				}
			} else if (first instanceof Enum) {
				// Enum Event
				if ((((Enum<?>)first).ordinal() < minEvent) || (((Enum<?>)first).ordinal() > maxEvent)
						|| (((Enum<?>)last).ordinal() < minEvent) || (((Enum<?>)last).ordinal() > maxEvent)) {
					message = String.format("Event must be %s (%d) to %s (%d). Received range: %s (%d) to %s (%d).",
							first.getClass().getEnumConstants()[minEvent].toString(), minEvent,
							first.getClass().getEnumConstants()[maxEvent].toString(), maxEvent,
							first.toString(), ((Enum<?>)first).ordinal(),
							last.toString(), ((Enum<?>)last).ordinal());
				}
			}
			if (message != null) {
				throw new IllegalArgumentException(message);
			}
		}
		EventData<A, S> data = new EventData<>(action, state);
		if (first instanceof Integer) {
			// Integer Event
			for (Integer i=(int)first; i<=(int)last; i++) {
				// Since E first is instanceof Integer, we know it is safe to cast Integer i to E.
				@SuppressWarnings("unchecked")
				E key = (E)i;
				events.put(key, data);
			}
		} else if (first instanceof Character) {
			// Character Event
			for (Character i=(char)first; i<=(char)last; i++) {
				// Since E first is instanceof Character, we know it is safe to cast Character i to E.
				@SuppressWarnings("unchecked")
				E key = (E)i;
				events.put(key, data);
			}
		} else if (first instanceof Enum) {
			// Enum Event
			// Since E first is instanceof Enum, E first.getClass() is E.class,
			// and E.class.getEnumConstants() will return E[], the cast should be safe.
			@SuppressWarnings("unchecked")
			E[] values = (E[])first.getClass().getEnumConstants();
			for (int i=((Enum<?>)first).ordinal(); i<=((Enum<?>)last).ordinal(); i++) {
				events.put(values[i], data);
			}
		} else {
			throw new IllegalArgumentException("StateData.addEvents() requires Integer, Character or Enum event type.");
		}
	}
	
	/**
	 * Checks if this State will respond to the given Event.
	 * 
	 * @param event Event to check for.
	 * @return True if this State has data for the given Event.
	 */
	public boolean hasEvent(E event) {
		return events.containsKey(event);
	}

	/**
	 * Returns the EventData (if any) for the given Event.
	 * 
	 * @param event Event to look up data for.
	 * @return EventData instance, or null.
	 */
	public EventData<A, S> getEvent(E event) {
		return events.get(event);
	}
}
