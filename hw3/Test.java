import hw2.SimulationManager;

public class Test {
  public static void main(String[] args) {
    thread(new Server(0.0001, 0.01, 165, 152.35, "EuCall", 252, "SimulationResult", 0.96, 0.01), false);
    thread(new Client(), false);
    //String str = "1.0E-4|0.01|165.0|152.35|EuCall|252|SimulationResultl";
    //System.out.println(str.split("\\|").length);
    
  }

  public static void thread(Runnable runnable, boolean daemon) {
    Thread brokerThread = new Thread(runnable);
    brokerThread.setDaemon(daemon);
    brokerThread.start();
  }
}
