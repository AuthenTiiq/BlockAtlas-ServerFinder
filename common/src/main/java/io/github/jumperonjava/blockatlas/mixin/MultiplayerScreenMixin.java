package io.github.jumperonjava.blockatlas.mixin;

import io.github.jumperonjava.blockatlas.BlockAtlasInit;
import io.github.jumperonjava.blockatlas.VoteLink;
import io.github.jumperonjava.blockatlas.gui.ServerScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    @Shadow private ServerInfo selectedEntry;
    @Shadow private ServerList serverList;
    @Shadow protected MultiplayerServerListWidget serverListWidget;
    @Shadow private ButtonWidget buttonEdit;
    @Mutable
    @Shadow @Final private Screen parent;
    private ButtonWidget voteButton;

    protected MultiplayerScreenMixin() {
        super(Text.empty());
    }
    @Inject(method = "connect(Lnet/minecraft/client/network/ServerInfo;)V",at = @At("HEAD"))
    public void disconnectFromServer(ServerInfo entry, CallbackInfo ci){
        this.parent = new TitleScreen();
        BlockAtlasInit.disconnect();
    }

    @Inject(method = "init", at=@At("HEAD"))
    void addButton(CallbackInfo ci){
        BlockAtlasInit.initApi();
    }
    @Inject(method = "init", at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/widget/AxisGridWidget;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;",ordinal = 1,shift = At.Shift.AFTER),locals = LocalCapture.CAPTURE_FAILHARD)
    void addButtonToAxis(CallbackInfo ci, ButtonWidget buttonWidget, ButtonWidget buttonWidget2, ButtonWidget buttonWidget3, ButtonWidget buttonWidget4, DirectionalLayoutWidget directionalLayoutWidget, AxisGridWidget axisGridWidget){
        axisGridWidget.add(addDrawableChild(new ButtonWidget.Builder(Text.translatable("blockatlas.findservers"), (x)-> {
            var screen = new ServerScreen(BlockAtlasInit.api);
            client.setScreen(screen);
        })
                .width(100).build()));
    }
    @Inject(method = "init", at = @At(value = "INVOKE",ordinal = 3, shift = At.Shift.BEFORE,target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    void addVoteButtonBeforeEdit(CallbackInfo ci){
        this.voteButton = addDrawableChild(new ButtonWidget.Builder(Text.translatable("blockatlas.vote"),(b)->{
            try {
                MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
                if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
                    ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry) entry).getServer();
                    var shouldVote = serverInfo != null && ((VoteLink) serverInfo).getVoteLink() != null;
                    if(shouldVote)
                    Util.getOperatingSystem().open(new URL(((VoteLink) serverInfo).getVoteLink()));
                    new Timer().schedule(new TimerTask(){

                        @Override
                        public void run() {
                            b.setFocused(false);
                        }
                    },50);
                }
            } catch (Exception ignore) {
            }
        }).dimensions(width/2-206,1000000,100,20).build());
    }
    @Inject(method = "render",at = @At("HEAD") )
    void moveVoteButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci){
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
            ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry) entry).getServer();
            var shouldVote = serverInfo != null && ((VoteLink)serverInfo).getVoteLink() != null;
            voteButton.setY(shouldVote?height-30:1000000);
            buttonEdit.setY(!shouldVote?height-30:1000000);

        }
    }

    @ModifyArgs(method = "init", at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/widget/AxisGridWidget;<init>(IILnet/minecraft/client/gui/widget/AxisGridWidget$DisplayAxis;)V"))
    void modifyArgAxis(Args args){
        args.set(0, (Integer)args.get(0)+104);
    }
    @ModifyArgs(method = "init", at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;width(I)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    void modifyArgWidth(Args args){
        args.set(0,100);
    }


}
