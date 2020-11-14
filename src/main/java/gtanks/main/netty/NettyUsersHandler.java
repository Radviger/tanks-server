package gtanks.main.netty;

import gtanks.logger.Logger;
import gtanks.logger.remote.RemoteDatabaseLogger;
import gtanks.main.netty.blackip.model.BlackIPsModel;
import org.jboss.netty.channel.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class NettyUsersHandler extends SimpleChannelUpstreamHandler {
    private static BlackIPsModel blackList = new BlackIPsModel();
    private NettyUsersHandlerController controller = new NettyUsersHandlerController();

    public static void block(String ip) {
        ip = ip.split(":")[0];
        blackList.block(ip);
    }

    public static void unblock(String ip) {
        ip = ip.split(":")[0];
        blackList.unblock(ip);
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (blackList.contains(ctx.getChannel().getRemoteAddress().toString().split(":")[0])) {
            ctx.getChannel().close();
        } else {
            this.controller.onClientConnected(ctx);
            this.log("Client connected from " + ctx.getChannel().getRemoteAddress() + " (" + ctx.getChannel().getId() + ")");
        }
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        this.controller.onClientDisconnect(ctx);
        this.log("Connection closed from " + ctx.getChannel().getRemoteAddress() + " (" + ctx.getChannel().getId() + ")");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        this.controller.onMessageRecived(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        try {
            StringWriter sw = new StringWriter();
            e.getCause().printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            RemoteDatabaseLogger.error(exceptionAsString.toString());
            if (ctx.getChannel().isConnected()) {
                ctx.getChannel().close();
            }
        } catch (Exception var5) {
            var5.printStackTrace();
            RemoteDatabaseLogger.error(var5);
        }

    }

    private void log(String txt) {
        Logger.log("[Netty]: " + txt);
    }
}
