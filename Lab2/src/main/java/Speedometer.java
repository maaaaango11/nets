public class Speedometer implements Runnable {

    Receiver timedR;
    long lastCount;

    Speedometer(Receiver r) {
        timedR = r;
        lastCount = 0;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(Constants.timeoutMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long newCount = timedR.numberOfParts;
            if (lastCount < newCount) {
                long uSpeedbt = (((newCount - lastCount) * Constants.BMessageLength) / Byte.SIZE);
                long uSpeedKb = (((uSpeedbt * 1000) / Constants.timeoutMillis) / 1000);
                System.out.println("Upload speed w/ peer "+timedR.peer+"; approx: " + uSpeedKb + " Kbps");
                lastCount = newCount;
            } else if (lastCount == newCount) {
                System.out.println("Upload w/ peer "+timedR.peer+" complete");
                break;
            } else {
                System.out.println("last count < new count???");
                break;
            }
        }
    }
}
