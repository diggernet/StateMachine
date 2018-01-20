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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

/**
 * State Machine implementation.
 * <p>
 * To create a State Machine, extend this class, then use addState() calls to set up States, Events and Actions.
 * 
 * @author walton
 *
 * @param <A> Type of Action enum.
 * @param <S> Type of State enum.
 * @param <E> Type of Event objects.
 */
public class StateMachine<A extends Enum<?>, S extends Enum<?>, E> {
	/**
	 * Constant to use for ANYWHERE State, to make code more clear.
	 * Any transitions added to State ANYWHERE will be checked for Events regardless of current State.
	 */
	protected final S ANYWHERE = null;
	
	/**
	 * Constant to use for no State transition for an Event, to make code more clear.
	 */
	protected final S NO_TRANSITION = null;
	
	/**
	 * Constant to use for no Action for an Event, to make code more clear.
	 */
	protected final A NO_ACTION = null;
	
	/**
	 * Map of States in this StateMachine, and the associated StateData.
	 */
	private final Map<S, StateData<A, S, E>> states = new HashMap<>();

	/**
	 * ActionHandler used to process Actions.
	 */
	private final ActionHandler<A, S, E> handler;
	
	/**
	 * EventMapper used to lookup Events.
	 */
	private final EventMapper<E> mapper;
	
	/**
	 * Minimum Event value, for Integer, Character or Enum Events.
	 */
	private final E minEvent;

	/**
	 * Maximum Event value, for Integer, Character or Enum Events.
	 */
	private final E maxEvent;

	/**
	 * Initial State of StateMachine.
	 */
	private final S initialState;

	/**
	 * Current State of StateMachine.
	 */
	private S currentState;

	/**
	 * Create a new StateMachine instance.
	 * 
	 * @param state Initial State for the StateMachine.
	 * @param handler ActionHandler to use to process Actions.
	 */
	public StateMachine(S state, ActionHandler<A, S, E> handler) {
		this(state, handler, null, null, null);
	}

	/**
	 * Create a new StateMachine instance.
	 * 
	 * @param state Initial State for the StateMachine.
	 * @param handler ActionHandler to use to process Actions.
	 * @param mapper EventMapper to use to lookup Events.
	 */
	public StateMachine(S state, ActionHandler<A, S, E> handler, EventMapper<E> mapper) {
		this(state, handler, mapper, null, null);
	}
	
	/**
	 * Create a new StateMachine instance, if Event type is Integer, Character or Enum.
	 * <p>
	 * This constructor enables range checking in StateData.addEvent() and StateData.addEvents().
	 * 
	 * @param state Initial State for the StateMachine.
	 * @param handler ActionHandler to use to process Actions.
	 * @param minEvent Minimum Event value.
	 * @param maxEvent Maximum Event value.
	 * @throws IllegalArgumentException If called for Event type other than Integer, Character or Enum.
	 */
	public StateMachine(S state, ActionHandler<A, S, E> handler, E minEvent, E maxEvent) throws IllegalArgumentException {
		this(state, handler, null, minEvent, maxEvent);
	}
	
	/**
	 * Create a new StateMachine instance, if Event type is Integer, Character or Enum.
	 * <p>
	 * This constructor enables range checking in StateData.addEvent() and StateData.addEvents().
	 * 
	 * @param state Initial State for the StateMachine.
	 * @param handler ActionHandler to use to process Actions.
	 * @param mapper EventMapper to use to lookup Events.
	 * @param minEvent Minimum Event value.
	 * @param maxEvent Maximum Event value.
	 * @throws IllegalArgumentException If called for Event type other than Integer, Character or Enum.
	 */
	public StateMachine(S state, ActionHandler<A, S, E> handler, EventMapper<E> mapper, E minEvent, E maxEvent) throws IllegalArgumentException {
		this.initialState = state;
		this.currentState = state;
		this.handler = handler;
		this.mapper = mapper;
		// create a StateData instance just to validate minEvent and maxEvent
		@SuppressWarnings("unused")
		StateData<A, S, E> data = new StateData<>(null, null, null, minEvent, maxEvent);
		// if we get this far, minEvent and maxEvent are good
		this.minEvent = minEvent;
		this.maxEvent = maxEvent;
	}
	
	/**
	 * Add a State to the StateMachine.
	 * <p>
	 * Use State=ANYWHERE to set up Events which apply to any State.
	 * 
	 * @param state State to add.
	 * @param onEntry Action to take when entering State, or NO_ACTION.
	 * @param onExit Action to take when exiting State, or NO_ACTION.
	 * @param consumer Consumer which is called to initialize the State (in particular, to add Events to State).
	 */
	public void addState(S state, A onEntry, A onExit, Consumer<StateData<A, S, E>> consumer) {
		StateData<A, S, E> data;
		if ((minEvent == null) && (maxEvent == null)) {
			data = new StateData<>(state, onEntry, onExit);
		} else {
			data = new StateData<>(state, onEntry, onExit, minEvent, maxEvent);
		}
		consumer.accept(data);
		states.put(state, data);
	}
	
	/**
	 * Reset StateMachine to initial State.
	 */
	public void reset() {
		this.currentState = this.initialState;
	}
	
	/**
	 * Return the StateData for the given State.
	 * 
	 * @param state State to return data for, or ANYWHERE for data applied to any State.
	 * @return StateData instance for given State.
	 */
	public StateData<A, S, E> getStateData(S state) {
		return states.get(state);
	}
	
	/**
	 * Returns the Action and State transition details for a given State and Event.
	 * <p>
	 * If the given State doesn't recognize the given Event, looks for an Event
	 * which is valid for any State.
	 * 
	 * @param state State to look for Event in.
	 * @param event Event to get EventData for.
	 * @return
	 */
	public EventData<A, S> getEventData(S state, E event) {
		E lookup = (mapper == null) ? event : mapper.map(event);
		// if given state has EventData for this event, return it
		StateData<A, S, E> data = states.get(state);
		if ((data != null) && data.hasEvent(lookup)) {
			return data.getEvent(lookup);
		}
		// otherwise look for valid-anywhere EventData for this event
		data = states.get(ANYWHERE);
		if ((data != null) && data.hasEvent(lookup)) {
			return data.getEvent(lookup);
		}
		return null;
	}

	/**
	 * Handle the given Event, based on the current State.
	 * 
	 * @param event Event to process.
	 */
	public void handleEvent(E event) {
		EventData<A, S> data = getEventData(currentState, event);

		/* Perform up to three actions:
		 *   1. the exit action of the old state
		 *   2. the action associated with the event
		 *   3. the entry action of the new state
		 */
		A action;
		if (data.state != null) {
			action = states.get(currentState).onExit;
			if ((action != null) && (handler != null)) {
				handler.onExit(currentState, action);
			}
		}

		if ((data.action != null) && (handler != null)) {
			handler.onEvent(currentState, event, data.action);
		}

		if (data.state != null) {
			action = states.get(data.state).onEntry;
			if ((action != null) && (handler != null)) {
				handler.onEntry(data.state, action);
			}
			currentState = data.state;
		}
	}

	// http://webgraphviz.com
	/**
	 * Returns a DOT String representing the StateMachine.
	 * 
	 * @param showAnywhere True to show ANYWHERE as a State, false to add its data to each State.
	 * @return DOT String
	 */
	public String getDOT(boolean showAnywhere) {
		StringBuilder sb = new StringBuilder();
		sb.append("strict digraph StateMachine {\n");
		sb.append(String.format("\"%s\" -> \"%s\"\n", "start", initialState));
		for (Entry<S, StateData<A, S, E>> stateEntry : states.entrySet()) {
			// look for the first non-null key
			S state = stateEntry.getKey();
			if (state == null) {
				continue;
			}
			// use it to get all the enum values
			// Since S state extends Enum, S state.getClass() is S.class,
			// and S.class.getEnumConstants() will return S[], the cast should be safe.
			@SuppressWarnings("unchecked")
			S[] values = (S[])state.getClass().getEnumConstants();
			for (S s : values) {
				sb.append(String.format("\"%s\" [shape=box, style=rounded];\n", s));
			}
			// and then skip the rest
			break;
		}
		
		Set<Transition<A, S>> transSet = new HashSet<>();
		StateData<A, S, E> anywhere = states.get(ANYWHERE);
		for (Entry<S, StateData<A, S, E>> stateEntry : states.entrySet()) {
			S state = stateEntry.getKey();
			if (!showAnywhere) {
				if (state == ANYWHERE) {
					continue;
				}
				if (anywhere != null) {
					for (Entry<E, EventData<A, S>> eventEntry : anywhere.events.entrySet()) {
						EventData<A, S> eventData = eventEntry.getValue();
						Transition<A, S> trans = new Transition<>(state, eventData.state, eventData.action);
						transSet.add(trans);
					}
				}
			}
			for (Entry<E, EventData<A, S>> eventEntry : stateEntry.getValue().events.entrySet()) {
				EventData<A, S> eventData = eventEntry.getValue();
				Transition<A, S> trans = new Transition<>(state, eventData.state, eventData.action);
				transSet.add(trans);
			}
		}
		
		for (Transition<A, S> trans : transSet) {
			if (trans.to == NO_TRANSITION) {
				continue;
			}
			String stateName = (trans.from == ANYWHERE) ? "anywhere" : trans.from.toString();
			if (trans.action == NO_ACTION) {
				sb.append(String.format("\"%s\" -> \"%s\"\n", stateName, trans.to));
			} else {
				sb.append(String.format("\"%s\" -> \"%s\" [label=\"%s\"];\n", stateName, trans.to, trans.action));
			}
		}
		sb.append("}\n");
		return sb.toString();
	}
	
	/**
	 * Data object used for building DOT String.
	 * 
	 * @author walton
	 *
 * @param <A> Type of Action enum.
 * @param <S> Type of State enum.
	 */
	private static class Transition<A extends Enum<?>, S extends Enum<?>> {
		public final S from;
		public final S to;
		public final A action;
		
		public Transition(S from, S to, A action) {
			this.from = from;
			this.to = to;
			this.action = action;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((action == null) ? 0 : action.hashCode());
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Transition)) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Transition<A, S> other = (Transition<A, S>) obj;
			if (action == null) {
				if (other.action != null) {
					return false;
				}
			} else if (!action.equals(other.action)) {
				return false;
			}
			if (from == null) {
				if (other.from != null) {
					return false;
				}
			} else if (!from.equals(other.from)) {
				return false;
			}
			if (to == null) {
				if (other.to != null) {
					return false;
				}
			} else if (!to.equals(other.to)) {
				return false;
			}
			return true;
		}
	}
}
