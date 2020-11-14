package gtanks.system.restart;

import java.io.OutputStream;

class OmittingOutputStream extends OutputStream {
    @Override
    public void write(int b) {
    }
}
