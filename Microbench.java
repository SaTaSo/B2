package microbench;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Microbench {

    static int objectSize = 1;

    HashMap<String, GenericObject> population;

    public Microbench(int datasize) {

        population = new HashMap<>();

        for (int i = 0; i < datasize; i++) {
            population.put(String.valueOf(i), new GenericObject(i));
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public static void main(String[] arg) {
	LocalDateTime from = LocalDateTime.now();	
	System.out.println("start: "+from);
        if (arg.length == 3) {
            try {
                int dataSize = Integer.parseInt(arg[0].substring(0, arg[0].length() - 1)) * 1000000;
                int nOperations = Integer.parseInt(arg[2].substring(0, arg[2].length() - 1)) * 1000000;
                int readPercentage = Integer.parseInt(arg[1]);

                Random rand = new Random();
                ByteBuffer bufferPercentage = ByteBuffer.allocateDirect(nOperations * 4);
                ByteBuffer bufferAccess = ByteBuffer.allocateDirect(nOperations * 4);
                for (long i = 0; i < nOperations; i++) {
                    bufferPercentage.putInt(rand.nextInt(100));
                    bufferAccess.putInt(rand.nextInt(dataSize - 1));
                }
                bufferPercentage.flip();
                bufferAccess.flip();


                // Disable System.err printing to System.out
                PrintStream original = System.out;
                System.setErr(new NullPrintStream());

                Microbench dataSet = new Microbench(dataSize);

                // Timed task each 10s that prints the throughput in that time span
                RemindTask rmdtask = new RemindTask();
                Timer timer = new Timer(true);
                timer.scheduleAtFixedRate(rmdtask, 10000, 10000);

                for (int i = 0; i < nOperations; i++) {
                    // Get the element to perform the read/write operation upon

                    int ss = bufferAccess.getInt();
                   /* if(rmdtask.interval == 4){
                        dataSet.population.put(String.valueOf(ss), new GenericObject(ss));
                    }*/
                    if (bufferPercentage.getInt() < readPercentage) {
                        // Read Operation
                        GenericObject object = dataSet.population.
                                get(String.valueOf(ss));
                        System.err.println(Arrays.toString(object.obj));
                    } else {
                        // Write Operation

                        dataSet.population.put(String.valueOf(ss), new GenericObject(ss));
                        // object.obj =;
                    }
                    rmdtask.counter++;
                }
                timer.cancel();
                timer.purge();
                System.setErr(original);
                double mean = ((nOperations / rmdtask.timer) * 1000);
                System.out.println("[Mean/s] Throughput : " + mean);
		LocalDateTime to = LocalDateTime.now();
		System.out.println("end: " + to);
		System.out.println("execution time" + ChronoUnit.MILLIS.between(from,to));
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Input is not valid");
        }
    }

    static class GenericObject {

        public long[] obj;
        public int number;

        public GenericObject(int value) {
            obj = new long[128 * objectSize];
            number = value;
        }
    }

    static class NullPrintStream extends PrintStream {

        public NullPrintStream() {
            super(new NullByteArrayOutputStream());
        }

        private static class NullByteArrayOutputStream extends ByteArrayOutputStream {

            public void write(int b) {
                // do nothing
            }

            public void write(byte[] b, int off, int len) {
                // do nothing
            }

            public void writeTo(OutputStream out) throws IOException {
                // do nothing
            }

        }

    }

    static class RemindTask extends TimerTask {
        public int counter = 0;
        public int timer = 0;
public int interval =0;
        @Override
        public void run() {
            interval++;
            timer += 10000;
            System.out.println("[" + timer / 1000 + "s] Throughput: " + counter);
            counter = 0;
        }
    }
}
