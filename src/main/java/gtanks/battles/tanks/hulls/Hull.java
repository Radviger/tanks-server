package gtanks.battles.tanks.hulls;

public class Hull {
    public float mass;
    public float power;
    public float speed;
    public float turnSpeed;
    public float hp;

    public Hull(float mass, float power, float speed, float turnSpeed, float hp) {
        this.mass = mass;
        this.power = power;
        this.speed = speed;
        this.turnSpeed = turnSpeed;
        this.hp = hp;
    }
}
