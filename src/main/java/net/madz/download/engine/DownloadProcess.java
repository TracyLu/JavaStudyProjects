package net.madz.download.engine;

import net.madz.core.lifecycle.IState;

public class DownloadProcess implements IDownloadProcess {

    @Override
    public <S extends IState> S getState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void prepare() {
        // TODO Auto-generated method stub
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
    }

    @Override
    public void receive(long bytes) {
        // TODO Auto-generated method stub
    }

    @Override
    public void inactivate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void activate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
    }

    @Override
    public void err() {
        // TODO Auto-generated method stub
    }

    @Override
    public void remove(boolean both) {
        // TODO Auto-generated method stub
    }

    @Override
    public void restart() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }
}
