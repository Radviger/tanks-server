package gtanks.main.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import java.util.HashMap;

public class NettyUsersHandlerController extends HashMap<ChannelHandlerContext, ProtocolTransfer> implements Runnable {
    private static final long serialVersionUID = 4922899768061891423L;
    private final Thread _thread = new Thread(this);

    public NettyUsersHandlerController() {
        this._thread.setName("NettyUsersHandlerController THREAD");
        this._thread.start();
    }

    public void onClientConnected(ChannelHandlerContext ctx) {
        this.put(ctx, new ProtocolTransfer(ctx.getChannel(), ctx));
    }

    public void onClientDisconnect(ChannelHandlerContext ctx) {
        if (this.get(ctx) != null) {
            this.get(ctx).onDisconnect();
            this.remove(ctx);
        }
    }

    public void onMessageRecived(ChannelHandlerContext ctx, MessageEvent msg) {
        this.get(ctx).decryptProtocol((String) msg.getMessage());
    }

    @Override
    public void run() {
    }
}
