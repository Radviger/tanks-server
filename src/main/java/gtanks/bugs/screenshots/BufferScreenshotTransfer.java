package gtanks.bugs.screenshots;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BufferScreenshotTransfer {
    private byte[] bytes;

    public BufferScreenshotTransfer encryptPacket(String packet) {
        if (this.bytes == null) {
            this.bytes = new byte[packet.length()];
        }

        String[] parser = packet.split(",");

        for (int i = 0; i < parser.length; ++i) {
            this.bytes[i] = Byte.parseByte(parser[i]);
        }

        return this;
    }

    public void writeTo(File file) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(this.bytes);
        stream.flush();
        stream.close();
    }

    public void clear() {
        this.bytes = null;
    }
}
