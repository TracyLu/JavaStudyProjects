package net.madz.download.engine;

import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Activate;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Err;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Finish;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Inactivate;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Pause;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Prepare;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Receive;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Remove;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Restart;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Resume;
import static net.madz.download.engine.IDownloadProcess.TransitionEnum.Start;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.madz.core.lifecycle.IReactiveObject;
import net.madz.core.lifecycle.IState;
import net.madz.core.lifecycle.ITransition;
import net.madz.core.lifecycle.annotations.StateMachine;
import net.madz.core.lifecycle.annotations.StateSet;
import net.madz.core.lifecycle.annotations.Transition;
import net.madz.core.lifecycle.annotations.TransitionSet;
import net.madz.core.lifecycle.annotations.action.Corrupt;
import net.madz.core.lifecycle.annotations.action.End;
import net.madz.core.lifecycle.annotations.action.Recover;
import net.madz.core.lifecycle.annotations.action.Redo;
import net.madz.core.lifecycle.annotations.state.Corrupted;
import net.madz.core.lifecycle.annotations.state.Initial;
import net.madz.core.lifecycle.annotations.state.Running;
import net.madz.core.lifecycle.annotations.state.Stopped;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.engine.IDownloadProcess.TransitionEnum;

@StateMachine(states = @StateSet(StateEnum.class), transitions = @TransitionSet(TransitionEnum.class))
public interface IDownloadProcess extends Serializable, IReactiveObject {

    public static enum TransitionEnum implements ITransition {
        Prepare,
        Start,
        Receive,
        @Corrupt
        Inactivate,
        @Recover
        Activate,
        Pause,
        Finish,
        Err,
        Remove,
        @Redo
        Restart,
        Resume
    }

    public static enum StateEnum implements IState<IDownloadProcess, StateEnum> {
        @Initial
        New,
        @Running(priority = 1)
        Queued,
        @Running(priority = 0)
        Started,
        @Corrupted(recoverPriority = 0)
        InactiveStarted,
        @Corrupted(recoverPriority = 1)
        InactiveQueued,
        @Stopped
        Paused,
        @Stopped
        Finished,
        @Stopped
        Failed,
        @End
        Removed;

        static {
            New.transitionFunction.put(Prepare, Queued);
            New.transitionFunction.put(Remove, Removed);
            Queued.transitionFunction.put(Inactivate, InactiveQueued);
            Queued.transitionFunction.put(Start, Started);
            Queued.transitionFunction.put(Pause, Paused);
            Queued.transitionFunction.put(Remove, Removed);
            InactiveQueued.transitionFunction.put(Activate, Queued);
            Started.transitionFunction.put(Receive, Started);
            Started.transitionFunction.put(Pause, Paused);
            Started.transitionFunction.put(Inactivate, InactiveStarted);
            Started.transitionFunction.put(Err, Failed);
            Started.transitionFunction.put(Finish, Finished);
            Started.transitionFunction.put(Remove, Removed);
            InactiveStarted.transitionFunction.put(Activate, Queued);
            Paused.transitionFunction.put(Restart, New);
            Paused.transitionFunction.put(Resume, New);
            Paused.transitionFunction.put(Remove, Removed);
            Finished.transitionFunction.put(Remove, Removed);
            Finished.transitionFunction.put(Restart, New);
            Failed.transitionFunction.put(Restart, New);
            Failed.transitionFunction.put(Resume, New);
            Failed.transitionFunction.put(Remove, Removed);
        }
        final HashMap<TransitionEnum, StateEnum> transitionFunction = new HashMap<TransitionEnum, StateEnum>();

        @Override
        public Map<? extends ITransition, StateEnum> getTransitionFunction() {
            return Collections.unmodifiableMap(transitionFunction);
        }

        @Override
        public Set<? extends ITransition> getOutboundTransitions() {
            return transitionFunction.keySet();
        }
    }

    @Transition
    void prepare();

    @Transition
    void start();

    @Transition
    void receive(long bytes);

    @Transition
    void inactivate();

    @Transition
    void activate();

    @Transition
    void pause();

    @Transition
    void finish();

    @Transition
    void err();

    @Transition
    void remove(boolean both);

    @Transition
    void restart();

    @Transition
    void resume();
}
