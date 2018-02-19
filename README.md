# StateMachine
StateMachine is a Java implementation of a state machine.
It was inspired by the state machine in Joshua Haberman's [vtparse](https://github.com/haberman/vtparse).


## Maven configuration

		<dependency>
			<groupId>net.digger</groupId>
			<artifactId>state-machine</artifactId>
			<version>1.0.0</version>
		</dependency>


## Usage
* Extend the StateMachine class, providing enums for Action and State, and your Event type.
* Instantiate your class, providing and initial State and an Action handler.
* Call addState() to create states, providing a State, optional entry and exit Actions, and an initialization callback.
The callback will receive an instance of StateData, which is can use to add events to the state using addEvent(), providing an Event, an optional Action to take, and an optional State to transition to.
* When initialization is complete, you can use handleEvent() to process each Event, which will call your Action handler as appropriate.
* You can also call reset() to reset the state machine to its initial condition, or getDOT() to retrieve the current state machine as a [DOT](https://graphviz.gitlab.io/_pages/doc/info/lang.html) string, suitable for rendering with Graphviz.

[VTParser](https://github.com/diggernet/VTParser) is implemented using StateMachine.

## License
StateMachine is provided under the terms of the GNU LGPLv3.
