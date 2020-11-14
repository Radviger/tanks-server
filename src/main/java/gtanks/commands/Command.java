package gtanks.commands;

public class Command {
    public final Type type;
    public final String[] args;

    public Command(Type type, String[] args) {
        this.type = type;
        this.args = args;
    }

    @Override
    public String toString() {
        return type.toString() + " " + String.join("", args);
    }
}
