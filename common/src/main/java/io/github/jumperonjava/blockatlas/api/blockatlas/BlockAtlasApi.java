package io.github.jumperonjava.blockatlas.api.blockatlas;

import io.github.jumperonjava.blockatlas.api.*;
import io.github.jumperonjava.blockatlas.api.cachingapi.CachingTag;

import java.util.*;

public class BlockAtlasApi implements ServerApi {
    private ListHandler<BlockAtlasServer> serverHandler = new EmptyHandler();
    private ListHandler<Tag> tagHandler = new EmptyHandler();

    public BlockAtlasApi(){
        loadTags();
    }
    private void loadTags(){
        Json.getFromUrl("https://api.blockatlas.net/", this::firstResponse,()->{});
    }
    private void firstResponse(String json) {
        try{
            var response = Json.GSON.fromJson(json, BlockAtlasApiResponse.class);
            tagList.add(BlockAtlasTag.MAIN_PAGE_TAG);
            response.unique_tags.forEach(tagStr ->{
                //tagHandler.tryLoadMoreCallback(this::getTags);
                tagList.add(new CachingTag(new BlockAtlasTag(tagStr)));
            });
            tagList.forEach(tagHandler::addElement);
        }
        catch (Exception e){e.printStackTrace();}
    }

    List<Tag> tagList=new LinkedList<>();

    @Override
    public void setTagHandler(ListHandler<Tag> tagListHandler) {
        this.tagHandler = tagListHandler;
    }

    @Override
    public List<Tag> getTags() {
        return new ArrayList<>(tagList);
    }

    @Override
    public Tag getMainTag() {
        return BlockAtlasTag.MAIN_PAGE_TAG;
    }
}
