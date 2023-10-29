package io.github.jumperonjava.blockatlas.api;

public interface ListHandler<T> {
    void addElement(T server);
    void tryLoadMoreCallback(Runnable loadMoreCallback);
    void clearElements(boolean resetScroll);
    void sendError(Throwable error);
    default void refreshCallback(Runnable refreshCallback){}
}
