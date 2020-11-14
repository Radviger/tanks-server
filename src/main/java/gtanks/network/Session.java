package gtanks.network;

import gtanks.main.netty.ProtocolTransfer;
import org.jboss.netty.channel.ChannelHandlerContext;

public class Session {
    private final ProtocolTransfer protocolTransfer;
    private final ChannelHandlerContext context;

    public Session(ProtocolTransfer protocolTransfer, ChannelHandlerContext context) {
        this.protocolTransfer = protocolTransfer;
        this.context = context;
    }

    public ProtocolTransfer getTransfer() {
        return this.protocolTransfer;
    }

    public boolean sessionOpened() {
        return this.context.getChannel().isOpen();
    }

    public boolean connected() {
        return this.context.getChannel().isConnected();
    }

    public String getIp() {
        return this.context.getChannel().getRemoteAddress().toString();
    }
}
