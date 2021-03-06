package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.*;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.transitions.TransitioningTriggerBehaviour;
import com.github.oxo42.stateless4j.triggers.*;

public class StateConfiguration<S, T> {

    private static final FuncBoolean NO_GUARD = new FuncBoolean() {
        @Override
        public boolean call() {
            return true;
        }
    };
    private static final Action NO_ACTION = new Action() {
        @Override
        public void doIt() {
        }
    };
    private static final Action1<Object[]> NO_ACTION_N = new Action1<Object[]>() {
    	@Override
    	public void doIt(Object[] args) {
    	}
    };
    private final StateRepresentation<S, T> representation;
    private final Func2<S, StateRepresentation<S, T>> lookup;

    public StateConfiguration(final StateRepresentation<S, T> representation, final Func2<S, StateRepresentation<S, T>> lookup) {
        assert representation != null : "representation is null";
        assert lookup != null : "lookup is null";
        this.representation = representation;
        this.lookup = lookup;
    }

    /**
     * Accept the specified trigger and transition to the destination state
     *
     * @param trigger          The accepted trigger
     * @param destinationState The state that the trigger will cause a transition to
     * @return The reciever
     */
    public StateConfiguration<S, T> permit(T trigger, S destinationState) {
        enforceNotIdentityTransition(destinationState);
        return publicPermit(trigger, destinationState);
    }

    /**
     * Accept the specified trigger and transition to the destination state.
     * 
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of
     * the destination state.
     *
     * @param trigger          The accepted trigger
     * @param destinationState The state that the trigger will cause a transition to
     * @param action           The action to be performed "during" transition
     * @return The reciever
     */
    public StateConfiguration<S, T> permit(T trigger, S destinationState, final Action action) {
        enforceNotIdentityTransition(destinationState);
        return publicPermit(trigger, destinationState, action);
    }

    /**
     * Accept the specified trigger and transition to the destination state
     *
     * @param trigger          The accepted trigger
     * @param destinationState The state that the trigger will cause a transition to
     * @param guard            Function that must return true in order for the trigger to be accepted
     * @return The reciever
     */
    public StateConfiguration<S, T> permitIf(T trigger, S destinationState, FuncBoolean guard) {
        enforceNotIdentityTransition(destinationState);
        return publicPermitIf(trigger, destinationState, guard);
    }

    /**
     * Accept the specified trigger and transition to the destination state
     * 
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of
     * the destination state.
     *
     * @param trigger          The accepted trigger
     * @param destinationState The state that the trigger will cause a transition to
     * @param guard            Function that must return true in order for the trigger to be accepted
     * @param action           The action to be performed "during" transition
     * @return The reciever
     */
    public StateConfiguration<S, T> permitIf(T trigger, S destinationState, FuncBoolean guard, Action action) {
        enforceNotIdentityTransition(destinationState);
        return publicPermitIf(trigger, destinationState, guard, action);
    }

    /**
     * Accept the specified trigger, execute action and stay in state
     *
     * Applies to the current state only. No exit or entry actions will be
     * executed and the state will not change. The only thing that happens is
     * the execution of a given action.
     *
     * @param trigger The accepted trigger
     * @param action  The action to be performed
     * @return The reciever
     */
    public StateConfiguration<S, T> permitInternal(T trigger, Action action) {
        return permitInternalIf(trigger, NO_GUARD, action);
    }

    /**
     * Accept the specified trigger, execute action and stay in state
     *
     * Applies to the current state only. No exit or entry actions will be
     * executed and the state will not change. The only thing that happens is
     * the execution of a given action.
     * <p>
     * The action is only executed if the given guard returns true. Otherwise
     * this transition will not be taken into account (so it does not count
     * as 'ignore', then).
     *
     * @param trigger The accepted trigger
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @param action  The action to be performed
     * @return The reciever
     */
    public StateConfiguration<S, T> permitInternalIf(T trigger, FuncBoolean guard, Action action) {
        assert guard != null : "guard is null";
        assert action != null : "action is null";
        representation.addTriggerBehaviour(new InternalTriggerBehaviour<S, T>(
              trigger, guard, action));
        return this;
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     *
     * @param trigger The accepted trigger
     * @return The reciever
     */
    public StateConfiguration<S, T> permitReentry(T trigger) {
        return publicPermit(trigger, representation.getUnderlyingState());
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state).
     *
     * @param trigger The accepted trigger
     * @param action  The action to be performed "during" transition
     * @return The reciever
     */
    public StateConfiguration<S, T> permitReentry(T trigger, Action action) {
        return publicPermit(trigger, representation.getUnderlyingState(), action);
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     *
     * @param trigger The accepted trigger
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @return The reciever
     */
    public StateConfiguration<S, T> permitReentryIf(T trigger, FuncBoolean guard) {
        return publicPermitIf(trigger, representation.getUnderlyingState(), guard);
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state).
     *
     * @param trigger The accepted trigger
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @return The reciever
     */
    public StateConfiguration<S, T> permitReentryIf(T trigger, FuncBoolean guard, Action action) {
        return publicPermitIf(trigger, representation.getUnderlyingState(), guard, action);
    }

    /**
     * ignore the specified trigger when in the configured state
     *
     * @param trigger The trigger to ignore
     * @return The receiver
     */
    public StateConfiguration<S, T> ignore(T trigger) {
        return ignoreIf(trigger, NO_GUARD);
    }

    /**
     * ignore the specified trigger when in the configured state, if the guard returns true
     *
     * @param trigger The trigger to ignore
     * @param guard   Function that must return true in order for the trigger to be ignored
     * @return The receiver
     */
    public StateConfiguration<S, T> ignoreIf(T trigger, FuncBoolean guard) {
        assert guard != null : "guard is null";
        representation.addTriggerBehaviour(new InternalTriggerBehaviour<S, T>(trigger, guard, NO_ACTION));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param entryAction Action to execute
     * @return The receiver
     */
    public StateConfiguration<S, T> onEntry(final Action entryAction) {
        assert entryAction != null : "entryAction is null";
        return onEntry(new Action1<Transition<S, T>>() {
            @Override
            public void doIt(Transition<S, T> t) {
                entryAction.doIt();
            }
        });
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param entryAction Action to execute, providing details of the transition
     * @return The receiver
     */
    public StateConfiguration<S, T> onEntry(final Action1<Transition<S, T>> entryAction) {
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(new Action2<Transition<S, T>, Object[]>() {
            @Override
            public void doIt(Transition<S, T> arg1, Object[] arg2) {
                entryAction.doIt(arg1);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute
     * @return The receiver
     */
    public StateConfiguration<S, T> onEntryFrom(T trigger, final Action entryAction) {
        assert entryAction != null : "entryAction is null";
        return onEntryFrom(trigger, new Action1<Transition<S, T>>() {
            @Override
            public void doIt(Transition<S, T> arg1) {
                entryAction.doIt();
            }
        });
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @return The receiver
     */
    public StateConfiguration<S, T> onEntryFrom(T trigger, final Action1<Transition<S, T>> entryAction) {
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(trigger, new Action2<Transition<S, T>, Object[]>() {
            @Override
            public void doIt(Transition<S, T> arg1, Object[] arg2) {
                entryAction.doIt(arg1);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @return The receiver
     */
    public <TArg0> StateConfiguration<S, T> onEntryFrom(TriggerWithParameters1<TArg0, S, T> trigger, final Action1<TArg0> entryAction, final Class<TArg0> classe0) {
        assert entryAction != null : "entryAction is null";
        return onEntryFrom(trigger, new Action2<TArg0, Transition<S, T>>() {
            @Override
            public void doIt(TArg0 arg1, Transition<S, T> arg2) {
                entryAction.doIt(arg1);
            }
        }, classe0);
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @return The receiver
     */
    public <TArg0> StateConfiguration<S, T> onEntryFrom(TriggerWithParameters1<TArg0, S, T> trigger, final Action2<TArg0, Transition<S, T>> entryAction, final Class<TArg0> classe0) {
        assert trigger != null : "trigger is null";
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(trigger.getTrigger(), new Action2<Transition<S, T>, Object[]>() {
            @SuppressWarnings("unchecked")
            @Override
            public void doIt(Transition<S, T> t, Object[] arg2) {
                entryAction.doIt((TArg0) arg2[0], t);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<S, T> onEntryFrom(TriggerWithParameters2<TArg0, TArg1, S, T> trigger, final Action2<TArg0, TArg1> entryAction, final Class<TArg0> classe0, final Class<TArg1> classe1) {
        assert entryAction != null : "entryAction is null";
        return onEntryFrom(trigger, new Action3<TArg0, TArg1, Transition<S, T>>() {
            @Override
            public void doIt(TArg0 a0, TArg1 a1, Transition<S, T> t) {
                entryAction.doIt(a0, a1);
            }
        }, classe0, classe1);
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<S, T> onEntryFrom(TriggerWithParameters2<TArg0, TArg1, S, T> trigger, final Action3<TArg0, TArg1, Transition<S, T>> entryAction, final Class<TArg0> classe0, final Class<TArg1> classe1) {
        assert trigger != null : "trigger is null";
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(trigger.getTrigger(), new Action2<Transition<S, T>, Object[]>() {
            @SuppressWarnings("unchecked")
            @Override
            public void doIt(Transition<S, T> t, Object[] args) {
                entryAction.doIt(
                        (TArg0) args[0],
                        (TArg1) args[1], t);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param classe2     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @param <TArg2>     Type of the third trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> onEntryFrom(TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger, final Action3<TArg0, TArg1, TArg2> entryAction, final Class<TArg0> classe0, final Class<TArg1> classe1, final Class<TArg2> classe2) {
        assert entryAction != null : "entryAction is null";
        return onEntryFrom(trigger, new Action4<TArg0, TArg1, TArg2, Transition<S, T>>() {
            @Override
            public void doIt(TArg0 a0, TArg1 a1, TArg2 a2, Transition<S, T> t) {
                entryAction.doIt(a0, a1, a2);
            }
        }, classe0, classe1, classe2);
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param classe2     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @param <TArg2>     Type of the third trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> onEntryFrom(TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger, final Action4<TArg0, TArg1, TArg2, Transition<S, T>> entryAction, final Class<TArg0> classe0, final Class<TArg1> classe1, final Class<TArg2> classe2) {
        assert trigger != null : "trigger is null";
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(trigger.getTrigger(), new Action2<Transition<S, T>, Object[]>() {
            @SuppressWarnings("unchecked")
            @Override
            public void doIt(Transition<S, T> t, Object[] args) {
                entryAction.doIt(
                        (TArg0) args[0],
                        (TArg1) args[1],
                        (TArg2) args[2], t);
            }
        });
        return this;
    }

    /**
     * Specify an action that will execute when transitioning from the configured state
     *
     * @param exitAction Action to execute
     * @return The receiver
     */
    public StateConfiguration<S, T> onExit(final Action exitAction) {
        assert exitAction != null : "exitAction is null";
        return onExit(new Action1<Transition<S, T>>() {
            @Override
            public void doIt(Transition<S, T> arg1) {
                exitAction.doIt();
            }
        });
    }

    /**
     * Specify an action that will execute when transitioning from the configured state
     *
     * @param exitAction Action to execute
     * @return The receiver
     */
    public StateConfiguration<S, T> onExit(Action1<Transition<S, T>> exitAction) {
        assert exitAction != null : "exitAction is null";
        representation.addExitAction(exitAction);
        return this;
    }

    /**
     * Sets the superstate that the configured state is a substate of
     * <p>
     * Substates inherit the allowed transitions of their superstate.
     * When entering directly into a substate from outside of the superstate,
     * entry actions for the superstate are executed.
     * Likewise when leaving from the substate to outside the supserstate,
     * exit actions for the superstate will execute.
     *
     * @param superstate The superstate
     * @return The receiver
     */
    public StateConfiguration<S, T> substateOf(S superstate) {
        StateRepresentation<S, T> superRepresentation = lookup.call(superstate);
        representation.setSuperstate(superRepresentation);
        superRepresentation.addSubstate(representation);
        return this;
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @return The reciever
     */
    public StateConfiguration<S, T> permitDynamic(T trigger, final Func<S> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state).
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param action                   The action to be performed "during" transition
     * @return The receiver
     */
    public StateConfiguration<S, T> permitDynamic(T trigger, final Func<S> destinationStateSelector, Action action) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD, action);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param <TArg0>                  Type of the first trigger argument
     * @return The receiver
     */
    public <TArg0> StateConfiguration<S, T> permitDynamic(TriggerWithParameters1<TArg0, S, T> trigger, Func2<TArg0, S> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state). The parameter of the
     * trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @return The receiver
     */
    public <TArg0> StateConfiguration<S, T> permitDynamic(TriggerWithParameters1<TArg0, S, T> trigger,
    		Func2<TArg0, S> destinationStateSelector, Action1<TArg0> action) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD, action);
    }
    
    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<S, T> permitDynamic(
            TriggerWithParameters2<TArg0, TArg1, S, T> trigger,
            Func3<TArg0, TArg1, S> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }

    
    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state). The parameters of the
     * trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<S, T> permitDynamic(
            TriggerWithParameters2<TArg0, TArg1, S, T> trigger,
            Func3<TArg0, TArg1, S> destinationStateSelector, Action2<TArg0, TArg1> action) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD, action);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> permitDynamic(TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger, final Func4<TArg0, TArg1, TArg2, S> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }


    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state). The parameters of the
     * trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> permitDynamic(TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
    		final Func4<TArg0, TArg1, TArg2, S> destinationStateSelector,
    		final Action3<TArg0, TArg1, TArg2> action) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD, action);
    }
    
    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @return The reciever
     */
    public StateConfiguration<S, T> permitDynamicIf(T trigger, final Func<S> destinationStateSelector, FuncBoolean guard) {
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(trigger, new Func2<Object[], S>() {
            @Override
            public S call(Object[] arg0) {
                return destinationStateSelector.call();
            }
        }, guard, NO_ACTION_N);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of the destination state.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param action                   The action to be performed "during" transition
     * @return The receiver
     */
    public StateConfiguration<S, T> permitDynamicIf(T trigger, final Func<S> destinationStateSelector, FuncBoolean guard,
    		final Action action) {
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(trigger, new Func2<Object[], S>() {
            @Override
            public S call(Object[] arg0) {
                return destinationStateSelector.call();
            }
        }, guard, new Action1<Object[]>() {
            @SuppressWarnings("unchecked")
        	@Override
        	public void doIt(Object[] args) {
        		action.doIt();
        	}
        });
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param <TArg0>                  Type of the first trigger argument
     * @return The reciever
     */
    public <TArg0> StateConfiguration<S, T> permitDynamicIf(TriggerWithParameters1<TArg0, S, T> trigger, final Func2<TArg0, S> destinationStateSelector, FuncBoolean guard) {
        assert trigger != null : "trigger is null";
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(
                trigger.getTrigger(), new Func2<Object[], S>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public S call(Object[] args) {
                        return destinationStateSelector.call((TArg0) args[0]);

                    }
                },
                guard, NO_ACTION_N
        );
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of the destination state.
     * The parameter of the trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @return The receiver
     */
    public <TArg0> StateConfiguration<S, T> permitDynamicIf(TriggerWithParameters1<TArg0, S, T> trigger,final Func2<TArg0, S> destinationStateSelector, FuncBoolean guard,
    		final Action1<TArg0> action) {
        assert trigger != null : "trigger is null";
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(
                trigger.getTrigger(), new Func2<Object[], S>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public S call(Object[] args) {
                        return destinationStateSelector.call((TArg0) args[0]);

                    }
                },
                guard, new Action1<Object[]>() {
                    @SuppressWarnings("unchecked")
                	@Override
                	public void doIt(Object[] args) {
                		action.doIt((TArg0) args[0]);
                	}
                }
        );
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @return The reciever
     */
    public <TArg0, TArg1> StateConfiguration<S, T> permitDynamicIf(TriggerWithParameters2<TArg0, TArg1, S, T> trigger, final Func3<TArg0, TArg1, S> destinationStateSelector, FuncBoolean guard) {
        assert trigger != null : "trigger is null";
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(
                trigger.getTrigger(), new Func2<Object[], S>() {
                    @SuppressWarnings("unchecked")

                    @Override
                    public S call(Object[] args) {
                        return destinationStateSelector.call(
                                (TArg0) args[0],
                                (TArg1) args[1]);
                    }
                },
                guard, NO_ACTION_N
        );
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of the destination state.
     * The parameters of the trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<S, T> permitDynamicIf(TriggerWithParameters2<TArg0, TArg1, S, T> trigger, final Func3<TArg0, TArg1, S> destinationStateSelector, FuncBoolean guard,
    		final Action2<TArg0, TArg1> action) {
        assert trigger != null : "trigger is null";
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(
                trigger.getTrigger(), new Func2<Object[], S>() {
                    @SuppressWarnings("unchecked")

                    @Override
                    public S call(Object[] args) {
                        return destinationStateSelector.call(
                                (TArg0) args[0],
                                (TArg1) args[1]);
                    }
                },
                guard, new Action1<Object[]>() {
                    @SuppressWarnings("unchecked")
                	@Override
                	public void doIt(Object[] args) {
                		action.doIt(
                				(TArg0) args[0],
                				(TArg1) args[1]);
                	}
                }
        );
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     * @return The reciever
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> permitDynamicIf(TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
            final Func4<TArg0, TArg1, TArg2, S> destinationStateSelector, FuncBoolean guard) {
        assert trigger != null : "trigger is null";
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(
                trigger.getTrigger(), new Func2<Object[], S>() {
                    @SuppressWarnings("unchecked")

                    @Override
                    public S call(Object[] args) {
                        return destinationStateSelector.call(
                                (TArg0) args[0],
                                (TArg1) args[1],
                                (TArg2) args[2]
                        );
                    }
                }, guard, NO_ACTION_N
            );
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function.
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of the destination state.
     * The parameters of the trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     * @return The reciever
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> permitDynamicIf(TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
            final Func4<TArg0, TArg1, TArg2, S> destinationStateSelector, FuncBoolean guard, final Action3<TArg0, TArg1, TArg2> action) {
        assert trigger != null : "trigger is null";
        assert destinationStateSelector != null : "destinationStateSelector is null";
        return publicPermitDynamicIf(
                trigger.getTrigger(), new Func2<Object[], S>() {
                    @SuppressWarnings("unchecked")

                    @Override
                    public S call(Object[] args) {
                        return destinationStateSelector.call(
                                (TArg0) args[0],
                                (TArg1) args[1],
                                (TArg2) args[2]
                        );
                    }
                }, guard, new Action1<Object[]>() {
                    @SuppressWarnings("unchecked")
                	@Override
                	public void doIt(Object[] args) {
                		action.doIt(
                				(TArg0) args[0],
                				(TArg1) args[1],
                				(TArg2) args[2]);
                	}
                }
        );
    }

    void enforceNotIdentityTransition(S destination) {
        if (destination.equals(representation.getUnderlyingState())) {
            throw new IllegalStateException("Permit() (and PermitIf()) require that the destination state is not equal to the source state. To accept a trigger without changing state, use either ignore(), permitInternal() or permitReentry().");
        }
    }

    StateConfiguration<S, T> publicPermit(T trigger, S destinationState) {
        return publicPermitIf(trigger, destinationState, NO_GUARD, NO_ACTION);
    }

    StateConfiguration<S, T> publicPermit(T trigger, S destinationState, Action action) {
        return publicPermitIf(trigger, destinationState, NO_GUARD, action);
    }

    StateConfiguration<S, T> publicPermitIf(T trigger, S destinationState, FuncBoolean guard) {
        return publicPermitIf(trigger, destinationState, guard, NO_ACTION);
    }

    StateConfiguration<S, T> publicPermitIf(T trigger, S destinationState, FuncBoolean guard, Action action) {
        assert guard != null : "guard is null";
        assert action != null : "action is null";
        representation.addTriggerBehaviour(new TransitioningTriggerBehaviour<>(trigger, destinationState, guard, action));
        return this;
    }

    StateConfiguration<S, T> publicPermitDynamic(T trigger, Func2<Object[], S> destinationStateSelector) {
        return publicPermitDynamicIf(trigger, destinationStateSelector, NO_GUARD, NO_ACTION_N);
    }

    StateConfiguration<S, T> publicPermitDynamicIf(T trigger, Func2<Object[], S> destinationStateSelector, FuncBoolean guard, Action1<Object[]> action) {
        assert destinationStateSelector != null : "destinationStateSelector is null";
        assert guard != null : "guard is null";
        representation.addTriggerBehaviour(new DynamicTriggerBehaviour<>(trigger, destinationStateSelector, guard, action));
        return this;
    }
}
