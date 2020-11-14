package gtanks.network;

import gtanks.StringUtils;
import gtanks.commands.Command;
import gtanks.commands.Type;
import gtanks.logger.Logger;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @deprecated
 */
@Deprecated
public class Networker implements INetworker {
    public final String splitterCommands = "~";
    public Socket client;
    public SocketChannel channel;
    public int bytes;

    public Networker(Socket client) {
        this.client = client;
        this.channel = client.getChannel();

        try {
            this.channel.configureBlocking(true);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public String socketToString() {
        return this.client.getInetAddress().toString().substring(1) + ":" + this.client.getPort() + "(local port:" + this.client.getLocalPort() + ")";
    }

    @Override
    public String onCommand() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        this.bytes = this.channel.read(buffer);
        if (this.bytes > 0) {
            buffer.flip();
            return new String(buffer.array());
        } else {
            return "";
        }
    }

    @Override
    public boolean send(String msg) throws IOException {
        this.channel.write(ByteBuffer.wrap(msg.getBytes()));
        return true;
    }

    public boolean send(Type type, String... args) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append(type.toString());
        request.append(";");

        for (int i = 0; i < args.length - 1; ++i) {
            request.append(StringUtils.concatStrings(args[i], ";"));
        }

        request.append(StringUtils.concatStrings(args[args.length - 1], "~"));
        String requestFinal = request.toString();
        this.channel.write(ByteBuffer.wrap(requestFinal.getBytes()));
        request = null;
        requestFinal = null;
        return true;
    }

    public boolean send(Command command) throws IOException {
        this.send(command.type, command.args);
        return true;
    }

    @Override
    public void closeConnection() {
        try {
            this.channel.close();
        } catch (IOException var2) {
            Logger.log(gtanks.logger.Type.ERROR, var2.getMessage());
        }

    }
}
