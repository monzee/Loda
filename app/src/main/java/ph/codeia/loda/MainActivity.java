package ph.codeia.loda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    static class Loadables {
        @Loda.Async(123)
        String getId() {
            return "abcde";
        }

        @Loda.Got(123)
        void didGetId(String id) {}

        @Loda.Lazy(32)
        String getEmail() {
            return "foobar@example.com";
        }

        @Loda.Got(32)
        void didGetEmail(String email, Loda.Caught bugs) {}
    }

    interface Component {
        void inject(MainActivity activity);
    }


    private static final int INJECTOR = 10;
    private static final int ITEMS = 5;
    private static final int MORE = 7;

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
    void inject(Component injector) {
        injector.inject(this);
    }

    @Loda.Async(ITEMS)
    List<Integer> primes() throws InterruptedException {
        Thread.sleep(10000);
        return Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23);
    }

    @Loda.Got(ITEMS)
    void fetched(List<Integer> primes, Loda.Caught interrupted) {
        try {
            interrupted.rethrow();
            // show in an RV
        } catch (Exception e) {
            e.printStackTrace();
            // show error message
        }
    }

    @Loda.Async(MORE)
    Map<String, Set<Integer>> prettyComplicatedType() {
        return null;
    }

    @Loda.Got(MORE)
    void yesItIs(Map<String, Set<Integer>> xs, Loda.Caught errors) {
    }
}
