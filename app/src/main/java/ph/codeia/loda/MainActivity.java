package ph.codeia.loda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import static ph.codeia.loda.MainActivity.INJECTOR;

public class MainActivity extends AppCompatActivity {

    private interface Component {
        void inject(MainActivity activity);
    }

    private static final int INJECTOR = 0;
    private static final int ITEMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // new MainActivityLoda(this).prepare(this);
    }

    @Loda.Lazy(INJECTOR)
    Component daggerComponent() {
        // return DaggerComponent.builder().build()
        return null;
    }

    @Loda.Got(INJECTOR)
    void got(Component injector) {
        injector.inject(this);
    }

    @Loda.Async(ITEMS)
    List<Integer> primes() throws InterruptedException {
        Thread.sleep(10000);
        return Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23);
    }

    @Loda.Got(ITEMS)
    void got(List<Integer> primes, Loda.Caught interrupted) {
        try {
            interrupted.rethrow();
            // show in an RV
        } catch (Exception e) {
            e.printStackTrace();
            // show error message
        }
    }

}
