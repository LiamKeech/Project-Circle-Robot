package Code;

public class Command
{
    public int left;
    public int right;
    public int time;
    public long timestamp;

    //Please note:
    //max_motor_speed=17500.0 (positive or negative)
    //min_motor_speed=8000.0	(positive or negative)
    //max_motor_time=3100
    //min_motor_time=300
    //preferred_motor_time=800
	
//    public Code.Command(int l, int r, int t) { //from new sim
//        Random rand = new Random();
//        this.left = l - 25 + rand.nextInt(50);
//        this.right = r - 25 + rand.nextInt(50);
//        this.time = t - 10 + rand.nextInt(20);
//        left = Math.max(8000, Math.min(17500, left));
//        right = Math.max(8000, Math.min(17500, right));
//        time = Math.max(300, Math.min(3100, time));
//    }
//
//    public Code.Command(int l, int r, int t, long timestamp) {
//        Random rand = new Random();
//        this.left = l - 25 + rand.nextInt(50);
//        this.right = r - 25 + rand.nextInt(50);
//        //this.time = t - 10 + rand.nextInt(20);
//        left = Math.max(8000, Math.min(17500, left));
//        right = Math.max(8000, Math.min(17500, right));
//        time = Math.max(300, Math.min(3100, time));
//        this.timestamp = timestamp;
//    }

    public String toString()
    {
        return "D,l" + left + ",l" + right + "\n";
    }

    public Command(int l, int r, int t)
    {
        this.left = l;
        this.right = r;
        this.time = t;
    }

    public Command(int l, int r, int t, long timestamp)
    {
        this.left = l;
        this.right = r;
        this.time = t;
        this.timestamp = timestamp;
    }
}
