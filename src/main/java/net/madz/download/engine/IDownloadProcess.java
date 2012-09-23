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
        /**
         * Preconditions: 
         * 
         * 1. Request Validation Passed
         * 
         * 1.1 URL Format legal
         *  
         * 1.2 File Path legal
         *   
         * 1.2.1 File does not exist
         * 
         * 1.2.2 File can be created (write) under it's directory
         * 
         * 1.2.2.1 File's directory (exists and can be written OR does not exist but can be created under it's parent directory)
         * 
         * Postconditions:
         * 
         * 1. Download Task Meta-data file is created.
         * 
         * 2. URL, Folder, filename, thread number, state = "New" are stored in the Meta-data file.
         */
        @Initial
        New,
        
        /**
         * Preconditions:
         *
         * 1. Download Task Meta-data file exists.
         * 
         * 2. Meta-information in the meta-data file are properly set.
         * 
         * 2.1. URL format is legal.
         * 
         * 2.2.Folder and filename is legal, as defined in New.Postconditions.
         * 
         * 2.3.Thread Number is greater than 0 and less than 20
         * 
         * 2.4.State is set legally (New, Inactive)
         *  
         * Postconditions:
         * 
         * 1. Following information should be reset and set within the meta-data file. 
         * 
         * 1.1. Total length is set
         * 
         * 1.2. resumable is set
         * 
         * 1.3. segments number is set
         * 
         * 1.4. state is set to "Prepared".
         * 
         * 2. Download Task data file is created 
         *    or re-created with deleting the pre-existing file (application aborted before update status to Prepared).
         */ 
        @Running(priority = 1)
        Prepared,
        /**
         * Preconditions:
         * 
         * Postconditions:
         */
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
            New.transitionFunction.put(Prepare, Prepared);
            New.transitionFunction.put(Remove, Removed);
            Prepared.transitionFunction.put(Inactivate, InactiveQueued);
            Prepared.transitionFunction.put(Start, Started);
            Prepared.transitionFunction.put(Pause, Paused);
            Prepared.transitionFunction.put(Remove, Removed);
            InactiveQueued.transitionFunction.put(Activate, Prepared);
            Started.transitionFunction.put(Receive, Started);
            Started.transitionFunction.put(Pause, Paused);
            Started.transitionFunction.put(Inactivate, InactiveStarted);
            Started.transitionFunction.put(Err, Failed);
            Started.transitionFunction.put(Finish, Finished);
            Started.transitionFunction.put(Remove, Removed);
            InactiveStarted.transitionFunction.put(Activate, Prepared);
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
