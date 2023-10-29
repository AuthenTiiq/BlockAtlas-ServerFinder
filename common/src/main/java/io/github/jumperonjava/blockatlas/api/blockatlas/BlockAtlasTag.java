package io.github.jumperonjava.blockatlas.api.blockatlas;

import io.github.jumperonjava.blockatlas.api.*;
import net.minecraft.text.MutableText;
import io.github.jumperonjava.blockatlas.api.cachingapi.CachingTag;
import net.minecraft.text.Text;

public class BlockAtlasTag implements Tag {
    public static final Tag MAIN_PAGE_TAG = new CachingTag(new BlockAtlasTag(""){
        @Override
        public MutableText getDisplayName() {
            return Text.literal("All Servers");
        }

        @Override
        protected String getApiLink(int page) {
            return String.format("https://api.blockatlas.net/page-%d",page);
        }
    });
    private final String tagname;
    private int lastLoadedPage;
    private ListHandler<Server> handler;

    @Override
    public int hashCode() {
        return tagname.hashCode();
    }

    public BlockAtlasTag(String s) {
        tagname = s;
    }
    @Override
    public void setServersFromTag(ListHandler<Server> handler) {
        this.handler = handler;
        this.lastLoadedPage = 0;
        handler.clearElements(true);
        handler.tryLoadMoreCallback(this::loadForNextPage);
        loadForNextPage();
    }
    private void loadForNextPage() {
        lastLoadedPage++;
        var apilink = getApiLink(lastLoadedPage);
        Json.getFromUrl(apilink,this::loadServers,()->{});
    }
    private void loadServers(String s) {
        try{
            var blockAtlasApiResponse = Json.GSON.fromJson(s, BlockAtlasApiResponse.class);
            blockAtlasApiResponse.servers.forEach(handler::addElement);
            if(blockAtlasApiResponse.current_page==blockAtlasApiResponse.total_pages)
                handler.addElement(null);
        }
        catch (Exception e)
        {
            handler.sendError(e);
            e.printStackTrace();
        }
    }
    @Override
    public MutableText getDisplayName() {
        return Text.literal(tagname);
    }
    protected String getApiLink(int page){
        return String.format("https://api.blockatlas.net/minecraft-%s-servers/page-%d",tagname,page);
    }
}
