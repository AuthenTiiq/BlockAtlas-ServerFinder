package io.github.jumperonjava.blockatlas.api.cachingapi;

import net.minecraft.text.MutableText;
import io.github.jumperonjava.blockatlas.api.EmptyHandler;
import io.github.jumperonjava.blockatlas.api.ListHandler;
import io.github.jumperonjava.blockatlas.api.Server;
import io.github.jumperonjava.blockatlas.api.Tag;
import net.minecraft.text.Text;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CachingTag implements Tag {
    private final Tag tag;
    private final ProxyServerHandler proxy = new ProxyServerHandler();
    public CachingTag(Tag tag){
        this.tag = tag;
        tag.setServersFromTag(proxy);
    }
    @Override
    public MutableText getDisplayName() {
        return tag.getDisplayName();
    }
    @Override
    public void setServersFromTag(ListHandler<Server> handler) {
        handler.refreshCallback(()->{
            proxy.clear();
            proxy.setServersFromTag(handler);
            tag.setServersFromTag(proxy);
        });
        handler.clearElements(true);
        proxy.setServersFromTag(handler);
    }
    static class ProxyServerHandler implements ListHandler<Server> {
        private ListHandler<Server> realHandler = null;
        private final Set<Server> queue = new LinkedHashSet<>();
        private Runnable callback;

        private void setServersFromTag(ListHandler<Server> h){
            realHandler = h;
            clearElements(false);
            queue.forEach(realHandler::addElement);
            realHandler.tryLoadMoreCallback(callback);
        }

        @Override
        public void addElement(Server server) {
            this.queue.add(server);
            if(realHandler!=null)
                setServersFromTag(realHandler);
        }

        @Override
        public void tryLoadMoreCallback(Runnable loadMoreCallback) {
            if(realHandler == null)
            {
                this.callback = loadMoreCallback;
                return;
            }
            realHandler.tryLoadMoreCallback(loadMoreCallback);
        }

        @Override
        public void clearElements(boolean clearScroll) {
            if(realHandler == null){
                return;
            }
            realHandler.clearElements(clearScroll);
        }

        @Override
        public void sendError(Throwable error) {
            if(realHandler ==null)
                return;
            realHandler.sendError(error);
        }

        public void clear() {
            queue.clear();
        }
    }
}
